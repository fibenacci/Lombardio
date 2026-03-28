export TF_VAR_target_ip=$(get_env "BARE_METAL_IP" "127.0.0.1")
export TF_VAR_ssh_user=$(get_env "BARE_METAL_USER" "$USER")
setup_ssh_keys
