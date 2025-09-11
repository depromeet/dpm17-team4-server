variable "bucket_name" {
  description = "The name of the S3 bucket"
  type        = string
}

variable "rule" {
  description = "The ACL rule to apply to the bucket (e.g., 'private', 'public-read', 'public-read-write', 'authenticated-read')"
  type        = string
  validation {
    condition     = contains(["private", "public-read", "public-read-write", "authenticated-read"], var.rule)
    error_message = "rule must be one of: private, public-read, public-read-write, authenticated-read."
  }
}
