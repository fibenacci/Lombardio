#!/usr/bin/env bash

set -euo pipefail

# --- Core Utils (provided for hooks) ---
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="${ROOT}/.env"
KUBE_DIR="${ROOT}/.kube"
PROVIDER="${1:-hcloud}"
ACTION="${2:-apply}"

info()  { printf "\033[0;34m[provision]\033[0m %s\n" "$*"; }
error() { printf "\033[0;31m[error]\033[0m %s\n" "$*" >&2; exit 1; }
get_env() { grep "^$1=" "${ENV_FILE}" | cut -d '=' -f2 || echo "${2:-}"; }

setup_ssh_keys() {
  local key_base="${HOME}/.ssh/id_rsa"
  [[ ! -f "$key_base" ]] && key_base="${HOME}/.ssh/id_ed25519"
  
  if [[ -f "$key_base" ]]; then
    export TF_VAR_ssh_public_key=$(cat "${key_base}.pub")
    export TF_VAR_ssh_private_key=$(cat "${key_base}")
    export TF_VAR_ssh_private_key_path="${key_base}"
  else
    info "Warning: No standard SSH key found (id_rsa/id_ed25519)."
  fi
}

run_terraform() {
  local dir="$1"
  local action="$2"
  info "Running Terraform ${action} in ${dir#$ROOT/}..."
  pushd "$dir" >/dev/null
  terraform init -reconfigure
  terraform "${action}" -auto-approve
  popd >/dev/null
}

# --- 1. Identify Environment Adapter ---
# Find a directory starting with the provider name
ADAPTER_DIR=$(find "${ROOT}/infra/terraform/environments" -maxdepth 1 -type d -name "${PROVIDER}*" | head -n 1)
[[ -z "${ADAPTER_DIR}" ]] && error "No environment adapter found for: ${PROVIDER}"

# --- 2. Load Provider Specifications (Hook) ---
if [[ -f "${ADAPTER_DIR}/configure.sh" ]]; then
  info "Loading specifications from ${ADAPTER_DIR#$ROOT/}..."
  source "${ADAPTER_DIR}/configure.sh"
fi

# --- 3. Provision Infrastructure (Layer 1) ---
run_terraform "${ADAPTER_DIR}" "${ACTION}"

# --- 4. Post-Provisioning (Apply Only) ---
if [[ "${ACTION}" == "apply" ]]; then
    mkdir -p "${KUBE_DIR}"
    KUBECONFIG_FILE="${KUBE_DIR}/config-lombardio"
    
    info "Updating Kubeconfig..."
    # If the adapter defines a custom kubeconfig export, use it
    if declare -f export_kubeconfig > /dev/null; then
        export_kubeconfig "${KUBECONFIG_FILE}"
    else
        pushd "${ADAPTER_DIR}" >/dev/null
        terraform output -raw kubeconfig > "${KUBECONFIG_FILE}"
        popd >/dev/null
    fi
    
    export KUBECONFIG="${KUBECONFIG_FILE}"
    export TF_VAR_kubeconfig_path="${KUBECONFIG_FILE}"

    # 5. Provision Foundation (Layer 2)
    run_terraform "${ROOT}/infra/terraform/environments/production" "apply"

    # 6. Deploy Application (Layer 3)
    info "Rolling out Lombardio Services via Kustomize..."
    kubectl apply -k "${ROOT}/infra/k8/overlays/production"
    
    info "DONE! Lombardio is flying on ${PROVIDER}."
fi
