# NCP Provider Variables
variable "ncp_access_key" {
  description = "Naver Cloud Platform Access Key"
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.ncp_access_key) > 0
    error_message = "ncp_access_key must not be empty."
  }
}

variable "ncp_secret_key" {
  description = "Naver Cloud Platform Secret Key"
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.ncp_secret_key) > 0
    error_message = "ncp_secret_key must not be empty."
  }
}

variable "ncp_region" {
  description = "Naver Cloud Platform Region"
  type        = string
  default     = "KR"
}

# Common Variables
variable "ncp_environment" {
  description = "Naver Cloud Platform Environment"
  type        = string
}

# VPC
variable "vpc_cidr_block" {
  description = "The CIDR block for the VPC"
  type        = string
  validation {
    condition     = can(cidrnetmask(var.vpc_cidr_block))
    error_message = "vpc_cidr_block must be a valid CIDR (e.g., 10.0.0.0/16)."
  }
}

# Subnet
variable "subnets" {
  description = "A map of subnets to create"
  type = map(object({
    cidr_block  = string
    zone        = string
    subnet_type = string
    usage_type  = string
  }))
}

# Servers
variable "backend_servers" {
  description = "A map of backend servers to create"
  type = map(object({
    subnet_key          = string
    server_image_number = string
    server_spec_code    = string
    is_public           = bool
  }))
}

variable "bastion_servers" {
  description = "A map of backend basion servers to create"
  type = map(object({
    subnet_key          = string
    server_image_number = string
    server_spec_code    = string
    is_public           = bool
  }))
  validation {
    condition = alltrue([
      for _, v in var.bastion_servers : contains(keys(var.subnets), v.subnet_key)
    ])
    error_message = "Each bastion server must reference a valid subnet_key."
  }
}

# PostgreSQL
variable "postgresql" {
  description = "PostgreSQL configuration"
  type = object({
    username      = string
    password      = string
    database_name = string
    subnet_key    = string
  })
  sensitive = true
  validation {
    condition     = contains(keys(var.subnets), var.postgresql.subnet_key)
    error_message = "PostgreSQL must reference a valid subnet_key."
  }
}
