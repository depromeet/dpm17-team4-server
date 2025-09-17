variable "vpc_no" {
  description = "The ID of the VPC where the NAT Gateway will be created"
  type        = string
}

variable "subnet_no" {
  description = "The ID of the Subnet where the NAT Gateway will be created"
  type        = string
}

variable "zone" {
  description = "The zone where the NAT Gateway will be created"
  type        = string
}

variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}
