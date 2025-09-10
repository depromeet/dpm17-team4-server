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
  network_interface {
    network_interface_no = ncloud_network_interface.nic.id
    order                = 0
  }
}

resource "ncloud_public_ip" "public_ip" {
  for_each = var.is_public ? {
    "main" = true
  } : {}
  server_instance_no = ncloud_server.server.instance_no
}

resource "ncloud_network_interface" "nic" {
  # required
  subnet_no             = var.subnet_no
  access_control_groups = var.access_control_groups

  # optional
  name = "${var.name_prefix}-${var.key}-nic"
}
