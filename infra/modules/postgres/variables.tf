variable "service_name" {
  description = "The name of the PostgreSQL service"
  type        = string
}

variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}

variable "username" {
  description = "The username for the PostgreSQL database"
  type        = string
}

variable "password" {
  description = "The password for the PostgreSQL database"
  type        = string
  sensitive   = true
}

variable "vpc_no" {
  description = "The ID of the VPC where the PostgreSQL instance will be created"
  type        = string
}

variable "subnet_no" {
  description = "The ID of the subnet where the PostgreSQL instance will be created"
  type        = string
}

variable "client_cidr" {
  description = "CIDR block for clients allowed to connect to the PostgreSQL instance"
  type        = string
}

variable "database_name" {
  description = "The name of the default database to create"
  type        = string
}

# server spec options
variable "image_product_code" {
  description = "The product code for the PostgreSQL image"
  type        = string
  default     = "SW.VPGSL.OS.LNX64.ROCKY.0810.PGSQL.B050" # PostgreSQL 14.18 on Rocky Linux 8.10
}

variable "product_code" {
  description = "The product code for the PostgreSQL instance"
  type        = string
  default     = "SVR.VPGSL.DBSVR.STAND.C002.M008.NET.SSD.B050.G002" # STANDARD, vCPU 2EA, Memory 8GB
}

# ha options
variable "ha" {
  description = "Whether to enable high availability for the PostgreSQL instance"
  type        = bool
  default     = false
}

variable "multi_zone" {
  description = "Whether to deploy the PostgreSQL instance across multiple availability zones"
  type        = bool
  default     = null
}

variable "secondary_subnet_no" {
  description = "The ID of the secondary subnet for high availability"
  type        = string
  default     = null
}

variable "backup" {
  description = "Whether to enable automated backups for the PostgreSQL instance"
  type        = bool
  default     = false
}

variable "backup_file_retention_period" {
  description = "The number of days to retain backup files"
  type        = number
  default     = null
}

variable "backup_time" {
  description = "The daily time window for backups in HH:MM format (UTC)"
  type        = string
  default     = null
}

variable "backup_file_storage_count" {
  description = "The number of backup files to store"
  type        = number
  default     = null
}

variable "backup_file_compression" {
  description = "Whether to enable compression for backup files"
  type        = bool
  default     = null
}

variable "access_control_group_no_list" {
  description = "List of access control group numbers allowed to access the PostgreSQL instance"
  type        = list(string)
  default     = []
}
