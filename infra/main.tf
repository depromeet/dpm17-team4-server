terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

provider "ncloud" {
  access_key  = var.ncp_access_key
  secret_key  = var.ncp_secret_key
  region      = var.ncp_region
  support_vpc = true
}

locals {
  name_prefix = var.ncp_environment
}

module "vpc" {
  source          = "./modules/vpc"
  ipv4_cidr_block = var.vpc_cidr_block
  name_prefix     = local.name_prefix
}

module "subnets" {
  source   = "./modules/subnet"
  for_each = var.subnets

  vpc_id         = module.vpc.vpc_id
  subnet_cidr    = each.value.cidr_block
  zone           = each.value.zone
  network_acl_no = module.vpc.default_network_acl_no
  subnet_type    = each.value.subnet_type
  usage_type     = each.value.usage_type
  name_prefix    = local.name_prefix
  subnet_key     = each.key
}
