output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "vpc_no" {
  description = "Number of the VPC"
  value       = module.vpc.vpc_no
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

output "default_network_acl_no" {
  description = "The ID of the default network ACL"
  value       = module.vpc.default_network_acl_no
}

output "default_access_control_group_no" {
  description = "The ID of the default access control group"
  value       = module.vpc.default_access_control_group_no
}

output "default_public_route_table_no" {
  description = "The ID of the default route table"
  value       = module.vpc.default_public_route_table_no
}

output "default_private_route_table_no" {
  description = "The ID of the default private route table"
  value       = module.vpc.default_private_route_table_no
}

output "subnets" {
  description = "The ID of the Subnet"
  value = {
    for key, subnet in module.subnets : key => {
      subnet_id = subnet.subnet_id
    }
  }
}

output "private_key_file" {
  description = "Path to the private key file for SSH access"
  value       = module.management_key.private_key_file
}

output "server_ips" {
  description = "Public IP addresses of created servers"
  value = {
    for key, server in module.servers : key => {
      public_ip  = server.public_ip
      private_ip = server.private_ip
    }
  }
}
