terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_subnet" "subnet" {
  # required
  vpc_no         = var.vpc_no
  subnet         = var.subnet_cidr
  zone           = var.zone
  network_acl_no = var.network_acl_no
  subnet_type    = var.subnet_type # PUBLIC(Public) | PRIVATE(Private)

  // optional
  name       = "${var.name_prefix}-${replace(lower(var.subnet_key), "_", "-")}"
  usage_type = var.usage_type
}
