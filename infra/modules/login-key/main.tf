terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_login_key" "loginkey" {
  key_name = var.key_name
  lifecycle {
    prevent_destroy = true
  }
}

# # PEM 파일을 로컬에 저장
# # TODO: 추후 HashiCorp Vault 같은 비밀 관리 솔루션으로 이전 고려
# resource "local_file" "private_key" {
#   content         = ncloud_login_key.loginkey.private_key
#   filename        = "${path.root}/key/${var.key_name}.pem"
#   file_permission = "0400"
# }
