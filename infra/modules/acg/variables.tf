variable "vpc_no" {
  description = "The ID of the VPC where the ACG will be created"
  type        = string
}

variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}

variable "acg_name" {
  description = "The name of the ACG"
  type        = string
}

variable "inbound_rules" {
  description = "A map of inbound rules for the ACG"
  type = map(object({
    protocol    = string
    ip_block    = string
    port_range  = string
    description = string
  }))
  default = {}
}

variable "outbound_rules" {
  description = "A map of outbound rules for the ACG"
  type = map(object({
    protocol    = string
    ip_block    = string
    port_range  = string
    description = string
  }))
  default = {}
}
