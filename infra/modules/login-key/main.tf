terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

variable "key_name" {
  description = "The name of the login key"
  type        = string
}

resource "ncloud_login_key" "loginkey" {
  key_name = var.key_name
}

output "key_id" {
  description = "The ID of the Login Key"
  value       = ncloud_login_key.loginkey.id
}

output "private_key" {
  description = "The private key of the Login Key"
  value       = ncloud_login_key.loginkey.private_key
  sensitive   = true
}

output "fingerprint" {
  description = "The fingerprint of the Login Key"
  value       = ncloud_login_key.loginkey.fingerprint
}

output "key_name" {
  description = "The name of the Login Key"
  value       = ncloud_login_key.loginkey.key_name
}
