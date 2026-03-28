resource "null_resource" "k3s_install" {
  connection {
    type        = "ssh"
    user        = var.ssh_user
    private_key = var.ssh_private_key
    host        = var.target_ip
  }

  provisioner "remote-exec" {
    inline = [
      "curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC='--tls-san ${var.target_ip}' sh -",
      "sudo wait && sudo chmod 644 /etc/rancher/k3s/k3s.yaml"
    ]
  }
}

data "external" "kubeconfig" {
  depends_on = [null_resource.k3s_install]
  program = ["sh", "-c", "ssh -o StrictHostKeyChecking=no -i ${var.ssh_private_key_path} ${var.ssh_user}@${var.target_ip} 'sudo cat /etc/rancher/k3s/k3s.yaml | sed \"s/127.0.0.1/${var.target_ip}/g\"' > ${path.module}/kubeconfig.yaml && echo '{\"status\": \"ok\"}'"]
}

output "kubeconfig" {
  value     = abspath("${path.module}/kubeconfig.yaml")
  sensitive = false
}
