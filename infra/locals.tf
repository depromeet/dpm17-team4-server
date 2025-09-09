locals {
  # ACG 공통 룰 정의
  common_inbound_rules = {
    ssh = {
      protocol    = "TCP"
      ip_block    = "0.0.0.0/0"
      port_range  = "22"
      description = "accept 22 port"
    }
    http = {
      protocol    = "TCP"
      ip_block    = "0.0.0.0/0"
      port_range  = "80"
      description = "accept 80 port"
    }
    https = {
      protocol    = "TCP"
      ip_block    = "0.0.0.0/0"
      port_range  = "443"
      description = "accept 443 port"
    }
  }

  # Backend 룰
  backend_inbound_rules = merge(local.common_inbound_rules, {
    app = {
      protocol = "TCP"
      # ip_block    = var.vpc_cidr_block
      ip_block    = "0.0.0.0/0" # TODO: 임시로 전체 허용, VPC CIDR로 변경 필요
      port_range  = "8080"
      description = "accept 8080 port from VPC"
    }
  })

  # Outbound 룰 (모든 포트 허용)
  common_outbound_rules = {
    all = {
      protocol    = "TCP"
      ip_block    = "0.0.0.0/0"
      port_range  = "1-65535"
      description = "accept 1-65535 port"
    }
  }
}
