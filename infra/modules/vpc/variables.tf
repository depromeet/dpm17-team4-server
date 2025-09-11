variable "ipv4_cidr_block" {
  description = "The IPv4 CIDR block for the VPC"
  type        = string
  validation {
    condition     = can(cidrhost(var.ipv4_cidr_block, 0))
    error_message = "ipv4_cidr_block must be a valid CIDR (e.g., 10.0.0.0/16)."
  }
}

variable "name_prefix" {
  description = "Prefix for naming resources"
  type        = string
}
