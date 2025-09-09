terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_access_control_group" "acg" {
  # required
  vpc_no      = var.vpc_no

  # optional
  name        = "${var.name_prefix}-${var.acg_name}-acg"
}

resource "ncloud_access_control_group_rule" "acg-rule" {
  access_control_group_no = ncloud_access_control_group.acg.id

  dynamic "inbound" {
    for_each = var.inbound_rules
    content {
      protocol    = inbound.value.protocol
      ip_block    = inbound.value.ip_block
      port_range  = inbound.value.port_range
      description = inbound.value.description
    }
  }

  dynamic "outbound" {
    for_each = var.outbound_rules
    content {
      protocol    = outbound.value.protocol
      ip_block    = outbound.value.ip_block
      port_range  = outbound.value.port_range
      description = outbound.value.description
    }
  }
}
