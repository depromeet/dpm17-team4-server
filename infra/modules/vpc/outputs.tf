output "vpc_id" {
  description = "The ID of the VPC"
  value       = ncloud_vpc.vpc.id
}

output "vpc_no" {
  description = "The number of the VPC"
  value       = ncloud_vpc.vpc.vpc_no
}

output "default_network_acl_no" {
  description = "The ID of the default network ACL"
  value       = ncloud_vpc.vpc.default_network_acl_no
}

output "default_access_control_group_no" {
  description = "The ID of the default access control group"
  value       = ncloud_vpc.vpc.default_access_control_group_no
}

output "default_public_route_table_no" {
  description = "The ID of the default route table"
  value       = ncloud_vpc.vpc.default_public_route_table_no
}

output "default_private_route_table_no" {
  description = "The ID of the default private route table"
  value       = ncloud_vpc.vpc.default_private_route_table_no
}
