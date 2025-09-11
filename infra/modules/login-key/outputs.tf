output "key_id" {
  description = "The ID of the Login Key"
  value       = ncloud_login_key.loginkey.id
}

output "fingerprint" {
  description = "The fingerprint of the Login Key"
  value       = ncloud_login_key.loginkey.fingerprint
}

output "key_name" {
  description = "The name of the Login Key"
  value       = ncloud_login_key.loginkey.key_name
}
