variable "vpc_no" {
  description = "The ID of the VPC where the resources will be created"
  type        = string
}

variable "subnet_cidr" {
  description = "CIDR blocks for subnets by type and availability zone"
  type        = string
}

variable "zone" {
  description = "Availability zones mapping"
  type        = string
}

variable "network_acl_no" {
  description = "The ID of the network ACL"
  type        = string
}

variable "subnet_type" {
  // PUBLIC(Public) | PRIVATE(Private)
  description = "Type of the subnet (public, private, or isolated)"
  type        = string
  default     = "PUBLIC"
}

variable "usage_type" {
  /**
  Default GEN
  Accepted values
   - GEN (General)
   - LOADB (For LoadBalancer)
   - BM (For BareMetal)
   - NATGW (for NATGateway)
   */
  description = "The usage type of the subnet"
  type        = string
  default     = "GEN"
}

variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}

variable "subnet_key" {
  description = "A key to identify the subnet"
  type        = string
}
