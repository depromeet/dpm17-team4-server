output "bucket_id" {
  description = "The ID of the object storage bucket"
  value       = ncloud_objectstorage_bucket.bucket.id
}
