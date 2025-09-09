output "server_id" {
    description = "ID of the server instance"
    value       = ncloud_server.server.id
}

output "server_instance_no" {
    description = "Instance number of the server"
    value       = ncloud_server.server.instance_no
}

output "public_ip" {
    description = "Public IP address of the server"
    value       = ncloud_server.server.public_ip
}

output "private_ip" {
    description = "Private IP address of the server"
    value       = ncloud_server.server.private_ip
}
