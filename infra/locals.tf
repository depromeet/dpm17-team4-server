locals {
  # ACG 공통 룰 정의
  bastion_inbound_rules = {
    ssh = {
      protocol    = "TCP"
      ip_block    = "0.0.0.0/0"
      port_range  = "22"
      description = "accept 22 port"
    }
  }

  # Nginx 룰
  nginx_inbound_rules = {
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
  backend_inbound_rules = {
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

  # Database 룰
  database_inbound_rules = {
    postgres = {
      protocol    = "TCP"
      ip_block    = var.vpc_cidr_block
      port_range  = "5432"
      description = "accept 5432 port from VPC"
    }
  }

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
