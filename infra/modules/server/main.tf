terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_server" "server" {
  # required
  subnet_no = var.subnet_no

  # optional
  name                = "${var.name_prefix}-${var.key}"
  server_image_number = var.server_image_number
  server_spec_code    = var.server_spec_code
  login_key_name      = var.login_key_name
}

resource "ncloud_public_ip" "public_ip" {
  for_each = var.is_public ? {
    "main" = true
  } : {}
  server_instance_no = ncloud_server.server.instance_no
}
