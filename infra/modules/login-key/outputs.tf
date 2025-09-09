output "key_id" {
  description = "The ID of the Login Key"
  value       = ncloud_login_key.loginkey.id
}

output "private_key" {
  description = "The private key of the Login Key"
  value       = ncloud_login_key.loginkey.private_key
  sensitive   = true
}

output "fingerprint" {
  description = "The fingerprint of the Login Key"
  value       = ncloud_login_key.loginkey.fingerprint
}

output "key_name" {
  description = "The name of the Login Key"
  value       = ncloud_login_key.loginkey.key_name
}

output "private_key_file" {
  description = "Path to the private key file"
  value       = local_file.private_key.filename
}
