###############################################################################
# Terraform Outputs for Loyalty Backend Support Chat Infrastructure
###############################################################################

###############################################################################
# General Information
###############################################################################

output "aws_region" {
  description = "AWS region where resources are created"
  value       = var.aws_region
}

output "environment" {
  description = "Environment name"
  value       = var.environment
}

output "aws_account_id" {
  description = "AWS Account ID"
  value       = data.aws_caller_identity.current.account_id
}

###############################################################################
# DynamoDB Outputs
###############################################################################

output "dynamodb_table_name" {
  description = "Name of the DynamoDB table for chat messages"
  value       = aws_dynamodb_table.chat_messages.name
}

output "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table"
  value       = aws_dynamodb_table.chat_messages.arn
}

output "dynamodb_table_id" {
  description = "ID of the DynamoDB table"
  value       = aws_dynamodb_table.chat_messages.id
}

output "dynamodb_stream_arn" {
  description = "ARN of the DynamoDB stream (if enabled)"
  value       = var.enable_dynamodb_streams ? aws_dynamodb_table.chat_messages.stream_arn : null
}

output "dynamodb_gsi_name" {
  description = "Name of the Global Secondary Index"
  value       = "ChatId-Timestamp-index"
}

###############################################################################
# S3 Outputs
###############################################################################

output "s3_bucket_name" {
  description = "Name of the S3 bucket for chat attachments"
  value       = aws_s3_bucket.chat_attachments.bucket
}

output "s3_bucket_arn" {
  description = "ARN of the S3 bucket"
  value       = aws_s3_bucket.chat_attachments.arn
}

output "s3_bucket_id" {
  description = "ID of the S3 bucket"
  value       = aws_s3_bucket.chat_attachments.id
}

output "s3_bucket_region" {
  description = "Region of the S3 bucket"
  value       = aws_s3_bucket.chat_attachments.region
}

output "s3_bucket_domain_name" {
  description = "Domain name of the S3 bucket"
  value       = aws_s3_bucket.chat_attachments.bucket_domain_name
}

###############################################################################
# SQS Outputs
###############################################################################

output "sqs_queue_url" {
  description = "URL of the SQS queue for chat notifications"
  value       = var.enable_sqs ? aws_sqs_queue.chat_notifications[0].url : null
}

output "sqs_queue_arn" {
  description = "ARN of the SQS queue"
  value       = var.enable_sqs ? aws_sqs_queue.chat_notifications[0].arn : null
}

output "sqs_dlq_url" {
  description = "URL of the SQS Dead Letter Queue"
  value       = var.enable_sqs ? aws_sqs_queue.chat_notifications_dlq[0].url : null
}

output "sqs_dlq_arn" {
  description = "ARN of the SQS Dead Letter Queue"
  value       = var.enable_sqs ? aws_sqs_queue.chat_notifications_dlq[0].arn : null
}

###############################################################################
# SNS Outputs
###############################################################################

output "sns_topic_arn" {
  description = "ARN of the SNS topic for chat alerts"
  value       = var.enable_sns ? aws_sns_topic.chat_alerts[0].arn : null
}

output "sns_topic_name" {
  description = "Name of the SNS topic"
  value       = var.enable_sns ? aws_sns_topic.chat_alerts[0].name : null
}

###############################################################################
# IAM Outputs
###############################################################################

output "iam_user_name" {
  description = "Name of the IAM user for backend application"
  value       = aws_iam_user.backend_user.name
}

output "iam_user_arn" {
  description = "ARN of the IAM user"
  value       = aws_iam_user.backend_user.arn
}

output "iam_access_key_id" {
  description = "Access Key ID for the IAM user"
  value       = aws_iam_access_key.backend_user.id
  sensitive   = true
}

output "iam_secret_access_key" {
  description = "Secret Access Key for the IAM user"
  value       = aws_iam_access_key.backend_user.secret
  sensitive   = true
}

###############################################################################
# Policy ARNs
###############################################################################

output "dynamodb_policy_arn" {
  description = "ARN of the DynamoDB access policy"
  value       = aws_iam_policy.dynamodb_access.arn
}

output "s3_policy_arn" {
  description = "ARN of the S3 access policy"
  value       = aws_iam_policy.s3_access.arn
}

output "sqs_policy_arn" {
  description = "ARN of the SQS access policy"
  value       = var.enable_sqs ? aws_iam_policy.sqs_access[0].arn : null
}

output "sns_policy_arn" {
  description = "ARN of the SNS access policy"
  value       = var.enable_sns ? aws_iam_policy.sns_access[0].arn : null
}

###############################################################################
# Environment Variables for Application
###############################################################################

output "application_env_vars" {
  description = "Environment variables to add to your .env file"
  value = <<-EOT

  # AWS Configuration - Add these to your .env file
  AWS_REGION=${var.aws_region}
  AWS_ACCESS_KEY_ID=${aws_iam_access_key.backend_user.id}
  AWS_SECRET_ACCESS_KEY=${aws_iam_access_key.backend_user.secret}

  # DynamoDB Configuration
  AWS_DYNAMODB_CHAT_TABLE=${aws_dynamodb_table.chat_messages.name}

  # S3 Configuration
  AWS_S3_BUCKET_NAME=${aws_s3_bucket.chat_attachments.bucket}
  AWS_S3_PRESIGNED_URL_DURATION=3600

  # SQS Configuration (if enabled)
  ${var.enable_sqs ? "AWS_SQS_QUEUE_URL=${aws_sqs_queue.chat_notifications[0].url}" : "# AWS_SQS_QUEUE_URL="}

  # SNS Configuration (if enabled)
  ${var.enable_sns ? "AWS_SNS_TOPIC_ARN=${aws_sns_topic.chat_alerts[0].arn}" : "# AWS_SNS_TOPIC_ARN="}
  EOT
  sensitive = true
}

###############################################################################
# Summary Output
###############################################################################

output "deployment_summary" {
  description = "Summary of deployed resources"
  value = {
    region              = var.aws_region
    environment         = var.environment
    dynamodb_table      = aws_dynamodb_table.chat_messages.name
    dynamodb_billing    = var.dynamodb_billing_mode
    s3_bucket           = aws_s3_bucket.chat_attachments.bucket
    sqs_enabled         = var.enable_sqs
    sns_enabled         = var.enable_sns
    autoscaling_enabled = var.enable_autoscaling && var.dynamodb_billing_mode == "PROVISIONED"
    pitr_enabled        = var.enable_point_in_time_recovery
    lifecycle_enabled   = var.enable_s3_lifecycle
  }
}

###############################################################################
# Cost Estimation (Approximate Monthly Costs)
###############################################################################

output "estimated_monthly_cost" {
  description = "Estimated monthly costs in USD (approximate)"
  value = <<-EOT

  Estimated Monthly Costs (based on moderate usage):

  DynamoDB (${var.dynamodb_billing_mode}):
  ${var.dynamodb_billing_mode == "PROVISIONED" ? "  - Provisioned Capacity: ~$${(var.dynamodb_read_capacity * 0.00013 + var.dynamodb_write_capacity * 0.00065) * 730}" : "  - On-Demand Pricing: ~$5-20 (depends on usage)"}

  S3 Storage:
    - Standard Storage: ~$0.023 per GB/month
    - Requests: Variable based on usage
    - Data Transfer: First 1 GB free, then $0.09/GB

  ${var.enable_sqs ? "SQS:\n  - First 1M requests free, then $0.40 per million" : ""}
  ${var.enable_sns ? "SNS:\n  - First 1M publishes free, then $0.50 per million" : ""}

  Total Estimated Cost: $${var.dynamodb_billing_mode == "PROVISIONED" ? "5-15" : "10-30"}/month

  Note: Actual costs depend on usage patterns. Enable cost allocation tags for accurate tracking.
  EOT
}
