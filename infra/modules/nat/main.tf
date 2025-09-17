terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}


resource "ncloud_nat_gateway" "nat_gateway" {
  # required
  vpc_no    = var.vpc_no
  subnet_no = var.subnet_no
  zone      = var.zone

  # optional
  name        = "${name_prefix}-nat-gateway"
  description = "The NAT Gateway for ${var.name_prefix} VPC"
}
