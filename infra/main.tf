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

data "ncloud_server_image_numbers" "server_images" {
  output_file = "ncp_server_spec/server_image.json"
}

data "ncloud_server_specs" "server_specs" {
  output_file = "ncp_server_spec/server_spec.json"
}

data "ncloud_postgresql_image_products" "postgresql_images" {
  output_file = "ncp_server_spec/postgresql_image.json"
}

data "ncloud_postgresql_products" "postgresql_specs" {
  image_product_code = data.ncloud_postgresql_image_products.postgresql_images.image_product_list.0.product_code
  output_file        = "ncp_server_spec/postgresql_spec.json"
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

  vpc_no         = module.vpc.vpc_id
  subnet_cidr    = each.value.cidr_block
  zone           = each.value.zone
  network_acl_no = module.vpc.default_network_acl_no
  subnet_type    = each.value.subnet_type
  usage_type     = each.value.usage_type
  name_prefix    = local.name_prefix
  subnet_key     = each.key
}

module "management_key" {
  source   = "./modules/login-key"
  key_name = "${local.name_prefix}-management-key"
}

# Nginx Servers
module "nginx_servers" {
  source   = "./modules/server"
  for_each = var.nginx_servers

  subnet_no           = module.subnets[each.value.subnet_key].subnet_id
  server_image_number = each.value.server_image_number
  server_spec_code    = each.value.server_spec_code
  login_key_name      = module.management_key.key_name
  key                 = each.key
  name_prefix         = local.name_prefix
  is_public           = each.value.is_public
  access_control_groups = [
    module.nginx_acg.acg_id
  ]
}

# Nginx ACG
module "nginx_acg" {
  source         = "./modules/acg"
  vpc_no         = module.vpc.vpc_id
  name_prefix    = local.name_prefix
  acg_name       = "nginx"
  inbound_rules  = local.nginx_inbound_rules
  outbound_rules = local.common_outbound_rules
}

# Backend ACG
module "backend_acg" {
  source = "./modules/acg"

  vpc_no         = module.vpc.vpc_id
  name_prefix    = local.name_prefix
  acg_name       = "backend"
  inbound_rules  = local.backend_inbound_rules
  outbound_rules = local.common_outbound_rules
}

# Backend Servers
module "backend_servers" {
  source   = "./modules/server"
  for_each = var.backend_servers

  subnet_no           = module.subnets[each.value.subnet_key].subnet_id
  server_image_number = each.value.server_image_number
  server_spec_code    = each.value.server_spec_code
  login_key_name      = module.management_key.key_name
  key                 = each.key
  name_prefix         = local.name_prefix
  is_public           = each.value.is_public
  access_control_groups = [
    module.backend_acg.acg_id
  ]
}

# Bastion ACG
module "bastion_acg" {
  source = "./modules/acg"

  vpc_no         = module.vpc.vpc_id
  name_prefix    = local.name_prefix
  acg_name       = "bastion"
  inbound_rules  = local.bastion_inbound_rules
  outbound_rules = local.common_outbound_rules
}

# Bastion Server
module "bastion_host" {
  source   = "./modules/server"
  for_each = var.bastion_servers

  subnet_no           = module.subnets[each.value.subnet_key].subnet_id
  server_image_number = each.value.server_image_number
  server_spec_code    = each.value.server_spec_code
  login_key_name      = module.management_key.key_name
  key                 = each.key
  name_prefix         = local.name_prefix
  is_public           = each.value.is_public
  access_control_groups = [
    module.bastion_acg.acg_id
  ]
}

# PostgreSQL
module "postgresql" {
  source = "./modules/postgres"

  service_name  = local.name_prefix
  name_prefix   = local.name_prefix
  username      = var.postgresql.username
  password      = var.postgresql.password
  vpc_no        = module.vpc.vpc_id
  subnet_no     = module.subnets[var.postgresql.subnet_key].subnet_id
  client_cidr   = module.vpc.vpc_cidr_block
  database_name = var.postgresql.database_name
  access_control_group_no_list = [
    module.backend_acg.acg_id,
    module.nginx_acg.acg_id
  ]
}

module "static_resource_bucket" {
  source      = "./modules/object-storage"
  bucket_name = "${local.name_prefix}-static-resources"
  rule        = "public-read"
}
