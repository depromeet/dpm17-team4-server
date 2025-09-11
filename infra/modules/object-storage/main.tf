terraform {
  required_providers {
    ncloud = {
      source = "NaverCloudPlatform/ncloud"
    }
  }
  required_version = ">= 0.13"
}

resource "ncloud_objectstorage_bucket" "bucket" {
  # required
  bucket_name = "${var.name_prefix}-${var.bucket_name}"
}

resource "ncloud_objectstorage_bucket_acl" "bucket_acl" {
  # required
  bucket_name = ncloud_objectstorage_bucket.bucket.bucket_name
  rule        = var.rule //  "private", "public-read", "public-read-write", "authenticated-read"
}
