variable "subnet_no" {
  description = "The ID of the subnet where the server will be created"
  type        = string
}

variable "server_image_number" {
  description = "The image number for the server"
  type        = string
}

variable "server_spec_code" {
  description = "The specification code for the server"
  type        = string
}

variable "login_key_name" {
  description = "The name of the login key to be used for the server"
  type        = string
}

variable "name_prefix" {
  description = "A prefix to use for all resources created by this module"
  type        = string
}

variable "is_public" {
  description = "Whether to assign a public IP to the server"
  type        = bool
  default     = false
}

variable "key" {
  description = "A key to identify the server"
  type        = string
}

variable "access_control_groups" {
  description = "A list of access control group IDs to associate with the network interface"
  type        = list(string)
}
