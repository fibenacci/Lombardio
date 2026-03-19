#!/usr/bin/env bash
# ============================================================
# Lombardio – Terraform helper script
# Usage: ./infra/scripts/tf.sh [local|aws|gcp] [init|plan|apply|destroy|output]
# ============================================================

set -euo pipefail

ENV="${1:-local}"
CMD="${2:-plan}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TF_DIR="${ROOT}/infra/terraform/environment/${ENV}"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
info()  { echo -e "${BLUE}[tf]${NC} $*"; }
warn()  { echo -e "${YELLOW}[tf]${NC} $*"; }

[ -d "${TF_DIR}" ] || { echo "Unknown environment '${ENV}'. Available: local, aws, gcp"; exit 1; }

cd "${TF_DIR}"

case "${CMD}" in
  init)
    info "Initialising Terraform for '${ENV}'..."
    terraform init
    ;;
  plan)
    info "Planning '${ENV}'..."
    terraform plan -out=".tfplan"
    warn "Review the plan above, then run: $0 ${ENV} apply"
    ;;
  apply)
    info "Applying '${ENV}'..."
    if [ -f ".tfplan" ]; then
      terraform apply ".tfplan" && rm -f ".tfplan"
    else
      terraform apply -auto-approve
    fi
    terraform output
    ;;
  destroy)
    warn "This will destroy ALL resources in '${ENV}'."
    read -rp "Type 'yes' to confirm: " confirm
    [ "${confirm}" = "yes" ] && terraform destroy -auto-approve
    ;;
  output)  terraform output ;;
  fmt)     terraform fmt -recursive ../../ ;;
  validate)
    terraform validate
    info "Configuration is valid."
    ;;
  state)   terraform state list ;;
  *)
    echo "Usage: $0 [local|aws|gcp] [init|plan|apply|destroy|output|fmt|validate|state]"
    exit 1
    ;;
esac
