terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_vpc" "vpc" {
  # required
  ipv4_cidr_block = var.ipv4_cidr_block

  # optional
  name = "${var.name_prefix}-vpc"
}
