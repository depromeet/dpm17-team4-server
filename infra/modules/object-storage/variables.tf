variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}

variable "bucket_name" {
  description = "The name of the S3 bucket"
  type        = string
}

variable "rule" {
  description = "The ACL rule to apply to the bucket (e.g., 'private', 'public-read', 'public-read-write', 'authenticated-read')"
  type        = string
}
