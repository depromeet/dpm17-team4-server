variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}

variable "bucket_name" {
  description = "The name of the S3 bucket"
  type        = string
}

variable "rule" {
  description = "A map of rules to apply to the bucket policy"
  type        = string
}
