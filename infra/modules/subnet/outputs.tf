output "subnet_id" {
    description = "The ID of the Subnet"
    value       = ncloud_subnet.subnet.id
}

output "subnet_no" {
    description = "The number of the Subnet"
    value       = ncloud_subnet.subnet.subnet_no
}
