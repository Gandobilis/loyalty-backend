###############################################################################
# Terraform Variables for Loyalty Backend Support Chat Infrastructure
###############################################################################

###############################################################################
# General Configuration
###############################################################################

variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "loyalty-backend"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

###############################################################################
# DynamoDB Configuration
###############################################################################

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table for chat messages (leave empty for auto-generated)"
  type        = string
  default     = ""
}

variable "dynamodb_billing_mode" {
  description = "DynamoDB billing mode (PROVISIONED or PAY_PER_REQUEST)"
  type        = string
  default     = "PAY_PER_REQUEST"

  validation {
    condition     = contains(["PROVISIONED", "PAY_PER_REQUEST"], var.dynamodb_billing_mode)
    error_message = "Billing mode must be PROVISIONED or PAY_PER_REQUEST."
  }
}

variable "dynamodb_read_capacity" {
  description = "Read capacity units for DynamoDB table (used only if billing_mode = PROVISIONED)"
  type        = number
  default     = 5
}

variable "dynamodb_write_capacity" {
  description = "Write capacity units for DynamoDB table (used only if billing_mode = PROVISIONED)"
  type        = number
  default     = 5
}

variable "dynamodb_gsi_read_capacity" {
  description = "Read capacity units for DynamoDB GSI (used only if billing_mode = PROVISIONED)"
  type        = number
  default     = 5
}

variable "dynamodb_gsi_write_capacity" {
  description = "Write capacity units for DynamoDB GSI (used only if billing_mode = PROVISIONED)"
  type        = number
  default     = 5
}

variable "enable_autoscaling" {
  description = "Enable auto-scaling for DynamoDB (only applicable for PROVISIONED mode)"
  type        = bool
  default     = true
}

variable "dynamodb_autoscaling_read_max" {
  description = "Maximum read capacity for auto-scaling"
  type        = number
  default     = 100
}

variable "dynamodb_autoscaling_write_max" {
  description = "Maximum write capacity for auto-scaling"
  type        = number
  default     = 100
}

variable "enable_point_in_time_recovery" {
  description = "Enable point-in-time recovery for DynamoDB table"
  type        = bool
  default     = true
}

variable "enable_dynamodb_streams" {
  description = "Enable DynamoDB Streams for real-time processing"
  type        = bool
  default     = false
}

variable "enable_message_ttl" {
  description = "Enable Time-To-Live for automatic message expiration"
  type        = bool
  default     = false
}

variable "kms_key_arn" {
  description = "ARN of KMS key for DynamoDB encryption (leave empty for AWS managed key)"
  type        = string
  default     = ""
}

###############################################################################
# S3 Configuration
###############################################################################

variable "s3_bucket_name" {
  description = "Name of the S3 bucket for chat attachments (leave empty for auto-generated, must be globally unique)"
  type        = string
  default     = ""
}

variable "enable_s3_versioning" {
  description = "Enable versioning for S3 bucket"
  type        = bool
  default     = false
}

variable "s3_kms_key_arn" {
  description = "ARN of KMS key for S3 encryption (leave empty for AES256)"
  type        = string
  default     = ""
}

variable "cors_allowed_origins" {
  description = "List of allowed origins for CORS configuration"
  type        = list(string)
  default     = ["http://localhost:3000", "https://*.example.com"]
}

variable "enable_s3_lifecycle" {
  description = "Enable lifecycle policy for S3 bucket"
  type        = bool
  default     = true
}

variable "s3_glacier_transition_days" {
  description = "Number of days before transitioning objects to Glacier"
  type        = number
  default     = 90
}

variable "s3_expiration_days" {
  description = "Number of days before deleting objects"
  type        = number
  default     = 365
}

variable "enable_s3_logging" {
  description = "Enable access logging for S3 bucket"
  type        = bool
  default     = false
}

###############################################################################
# SQS Configuration
###############################################################################

variable "enable_sqs" {
  description = "Enable SQS queue for chat notifications"
  type        = bool
  default     = false
}

###############################################################################
# SNS Configuration
###############################################################################

variable "enable_sns" {
  description = "Enable SNS topic for chat alerts"
  type        = bool
  default     = false
}

variable "sns_kms_key_arn" {
  description = "ARN of KMS key for SNS encryption (leave empty for AWS managed key)"
  type        = string
  default     = ""
}

variable "sns_email_subscriptions" {
  description = "List of email addresses to subscribe to SNS alerts"
  type        = list(string)
  default     = []
}

###############################################################################
# CloudWatch Configuration
###############################################################################

variable "enable_cloudwatch_alarms" {
  description = "Enable CloudWatch alarms for monitoring"
  type        = bool
  default     = true
}

variable "s3_size_alarm_threshold_gb" {
  description = "Threshold in GB for S3 bucket size alarm"
  type        = number
  default     = 100
}

###############################################################################
# Tags
###############################################################################

variable "additional_tags" {
  description = "Additional tags to apply to all resources"
  type        = map(string)
  default     = {}
}
