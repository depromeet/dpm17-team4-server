# NCP Provider Variables
variable "ncp_access_key" {
  description = "Naver Cloud Platform Access Key"
  type        = string
}

variable "ncp_secret_key" {
  description = "Naver Cloud Platform Secret Key"
  type        = string
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
variable "servers" {
  description = "A map of servers to create"
  type = map(object({
    subnet_key          = string
    server_image_number = string
    server_spec_code    = string
  }))
}
