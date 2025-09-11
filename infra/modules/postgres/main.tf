terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_postgresql" "postgresql" {
  # required
  service_name       = var.service_name
  server_name_prefix = var.name_prefix
  user_name          = var.username
  user_password      = var.password
  vpc_no             = var.vpc_no
  subnet_no          = var.subnet_no
  client_cidr        = var.client_cidr
  database_name      = var.database_name

  # server spec
  image_product_code = var.image_product_code
  product_code       = var.product_code

  # optional
  ha         = var.ha
  multi_zone = var.ha ? var.multi_zone : null
  // multi_zone is only applicable if ha is true
  secondary_subnet_no = var.ha && var.multi_zone ? var.secondary_subnet_no : null
  // required if multi_zone is true

  backup                       = var.backup // if ha is true, backup is mandatory
  backup_file_retention_period = var.backup ? var.backup_file_retention_period : null
  backup_time                  = var.backup ? var.backup_time : null // required if backup is true
  backup_file_storage_count    = var.backup ? var.backup_file_storage_count : null
  backup_file_compression      = var.backup ? var.backup_file_compression : null
}


resource "ncloud_access_control_group_rule" "db_inbound_from_acgs" {
  for_each                = toset(ncloud_postgresql.postgresql.access_control_group_no_list)
  access_control_group_no = each.key

  dynamic "inbound" {
    for_each = var.access_control_group_no_list
    content {
      protocol                       = "TCP"
      source_access_control_group_no = inbound.value
      port_range                     = "5432"
      description                    = "Allow ACG ${inbound.value} to access PostgreSQL"
    }
  }
}
