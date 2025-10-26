###############################################################################
# Terraform Configuration for Loyalty Backend Support Chat Infrastructure
#
# This configuration provisions AWS resources for the support chat feature:
# - DynamoDB table for chat messages
# - S3 bucket for file attachments
# - IAM user and policies
# - SQS queue for notifications (optional)
# - SNS topic for push notifications (optional)
# - CloudWatch alarms for monitoring
#
# Author: Generated for loyalty-backend
# Version: 1.0
###############################################################################

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Optional: Configure remote state backend
  # backend "s3" {
  #   bucket         = "loyalty-backend-terraform-state"
  #   key            = "chat/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-state-lock"
  # }
}

# Configure AWS Provider
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Purpose     = "Support Chat Infrastructure"
    }
  }
}

###############################################################################
# Local Variables
###############################################################################

locals {
  # Naming conventions
  resource_prefix = "${var.project_name}-${var.environment}"

  # DynamoDB table name
  dynamodb_table_name = var.dynamodb_table_name != "" ? var.dynamodb_table_name : "${local.resource_prefix}-chat-messages"

  # S3 bucket name (must be globally unique)
  s3_bucket_name = var.s3_bucket_name != "" ? var.s3_bucket_name : "${local.resource_prefix}-chat-attachments-${data.aws_caller_identity.current.account_id}"

  # SQS queue name
  sqs_queue_name = "${local.resource_prefix}-chat-notifications"

  # SNS topic name
  sns_topic_name = "${local.resource_prefix}-chat-alerts"

  # IAM user name
  iam_user_name = "${local.resource_prefix}-backend-user"

  # Common tags
  common_tags = {
    Application = "Loyalty Backend"
    Component   = "Support Chat"
  }
}

# Get current AWS account ID
data "aws_caller_identity" "current" {}

# Get current AWS region
data "aws_region" "current" {}

###############################################################################
# DynamoDB Table for Chat Messages
###############################################################################

resource "aws_dynamodb_table" "chat_messages" {
  name           = local.dynamodb_table_name
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "MessageId"

  # Provisioned capacity (used only if billing_mode = "PROVISIONED")
  read_capacity  = var.dynamodb_billing_mode == "PROVISIONED" ? var.dynamodb_read_capacity : null
  write_capacity = var.dynamodb_billing_mode == "PROVISIONED" ? var.dynamodb_write_capacity : null

  # Primary key attribute
  attribute {
    name = "MessageId"
    type = "S"
  }

  # GSI partition key
  attribute {
    name = "ChatId"
    type = "N"
  }

  # GSI sort key
  attribute {
    name = "Timestamp"
    type = "N"
  }

  # Global Secondary Index for querying messages by chat
  global_secondary_index {
    name            = "ChatId-Timestamp-index"
    hash_key        = "ChatId"
    range_key       = "Timestamp"
    projection_type = "ALL"

    read_capacity  = var.dynamodb_billing_mode == "PROVISIONED" ? var.dynamodb_gsi_read_capacity : null
    write_capacity = var.dynamodb_billing_mode == "PROVISIONED" ? var.dynamodb_gsi_write_capacity : null
  }

  # Enable point-in-time recovery for production
  point_in_time_recovery {
    enabled = var.enable_point_in_time_recovery
  }

  # Enable encryption at rest
  server_side_encryption {
    enabled     = true
    kms_key_arn = var.kms_key_arn != "" ? var.kms_key_arn : null
  }

  # Enable TTL for automatic message expiration (optional)
  dynamic "ttl" {
    for_each = var.enable_message_ttl ? [1] : []
    content {
      enabled        = true
      attribute_name = "ExpirationTime"
    }
  }

  # Enable DynamoDB Streams for real-time processing (optional)
  dynamic "stream_specification" {
    for_each = var.enable_dynamodb_streams ? [1] : []
    content {
      enabled   = true
      stream_view_type = "NEW_AND_OLD_IMAGES"
    }
  }

  tags = merge(
    local.common_tags,
    {
      Name = local.dynamodb_table_name
    }
  )
}

# Auto-scaling for DynamoDB (only if billing_mode = "PROVISIONED")
resource "aws_appautoscaling_target" "dynamodb_table_read" {
  count              = var.dynamodb_billing_mode == "PROVISIONED" && var.enable_autoscaling ? 1 : 0
  max_capacity       = var.dynamodb_autoscaling_read_max
  min_capacity       = var.dynamodb_read_capacity
  resource_id        = "table/${aws_dynamodb_table.chat_messages.name}"
  scalable_dimension = "dynamodb:table:ReadCapacityUnits"
  service_namespace  = "dynamodb"
}

resource "aws_appautoscaling_policy" "dynamodb_table_read_policy" {
  count              = var.dynamodb_billing_mode == "PROVISIONED" && var.enable_autoscaling ? 1 : 0
  name               = "${local.dynamodb_table_name}-read-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.dynamodb_table_read[0].resource_id
  scalable_dimension = aws_appautoscaling_target.dynamodb_table_read[0].scalable_dimension
  service_namespace  = aws_appautoscaling_target.dynamodb_table_read[0].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "DynamoDBReadCapacityUtilization"
    }
    target_value = 70.0
  }
}

resource "aws_appautoscaling_target" "dynamodb_table_write" {
  count              = var.dynamodb_billing_mode == "PROVISIONED" && var.enable_autoscaling ? 1 : 0
  max_capacity       = var.dynamodb_autoscaling_write_max
  min_capacity       = var.dynamodb_write_capacity
  resource_id        = "table/${aws_dynamodb_table.chat_messages.name}"
  scalable_dimension = "dynamodb:table:WriteCapacityUnits"
  service_namespace  = "dynamodb"
}

resource "aws_appautoscaling_policy" "dynamodb_table_write_policy" {
  count              = var.dynamodb_billing_mode == "PROVISIONED" && var.enable_autoscaling ? 1 : 0
  name               = "${local.dynamodb_table_name}-write-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.dynamodb_table_write[0].resource_id
  scalable_dimension = aws_appautoscaling_target.dynamodb_table_write[0].scalable_dimension
  service_namespace  = aws_appautoscaling_target.dynamodb_table_write[0].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "DynamoDBWriteCapacityUtilization"
    }
    target_value = 70.0
  }
}

###############################################################################
# S3 Bucket for Chat Attachments
###############################################################################

resource "aws_s3_bucket" "chat_attachments" {
  bucket = local.s3_bucket_name

  tags = merge(
    local.common_tags,
    {
      Name = local.s3_bucket_name
    }
  )
}

# Enable versioning (optional, for file history)
resource "aws_s3_bucket_versioning" "chat_attachments" {
  bucket = aws_s3_bucket.chat_attachments.id

  versioning_configuration {
    status = var.enable_s3_versioning ? "Enabled" : "Disabled"
  }
}

# Enable encryption at rest
resource "aws_s3_bucket_server_side_encryption_configuration" "chat_attachments" {
  bucket = aws_s3_bucket.chat_attachments.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = var.s3_kms_key_arn != "" ? "aws:kms" : "AES256"
      kms_master_key_id = var.s3_kms_key_arn != "" ? var.s3_kms_key_arn : null
    }
  }
}

# Block all public access
resource "aws_s3_bucket_public_access_block" "chat_attachments" {
  bucket = aws_s3_bucket.chat_attachments.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# CORS configuration for frontend access
resource "aws_s3_bucket_cors_configuration" "chat_attachments" {
  bucket = aws_s3_bucket.chat_attachments.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = var.cors_allowed_origins
    expose_headers  = ["ETag"]
    max_age_seconds = 3600
  }
}

# Lifecycle policy for automatic file archival/deletion
resource "aws_s3_bucket_lifecycle_configuration" "chat_attachments" {
  count  = var.enable_s3_lifecycle ? 1 : 0
  bucket = aws_s3_bucket.chat_attachments.id

  rule {
    id     = "archive-old-attachments"
    status = "Enabled"

    # Transition to Glacier after 90 days
    transition {
      days          = var.s3_glacier_transition_days
      storage_class = "GLACIER"
    }

    # Delete after 365 days
    expiration {
      days = var.s3_expiration_days
    }

    # Clean up incomplete multipart uploads
    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# Enable bucket logging (optional)
resource "aws_s3_bucket" "logs" {
  count  = var.enable_s3_logging ? 1 : 0
  bucket = "${local.s3_bucket_name}-logs"

  tags = merge(
    local.common_tags,
    {
      Name = "${local.s3_bucket_name}-logs"
    }
  )
}

resource "aws_s3_bucket_logging" "chat_attachments" {
  count  = var.enable_s3_logging ? 1 : 0
  bucket = aws_s3_bucket.chat_attachments.id

  target_bucket = aws_s3_bucket.logs[0].id
  target_prefix = "s3-access-logs/"
}

###############################################################################
# SQS Queue for Notifications (Optional)
###############################################################################

resource "aws_sqs_queue" "chat_notifications" {
  count = var.enable_sqs ? 1 : 0

  name                       = local.sqs_queue_name
  delay_seconds              = 0
  max_message_size           = 262144 # 256 KB
  message_retention_seconds  = 345600 # 4 days
  receive_wait_time_seconds  = 10     # Long polling
  visibility_timeout_seconds = 30

  # Enable encryption
  sqs_managed_sse_enabled = true

  tags = merge(
    local.common_tags,
    {
      Name = local.sqs_queue_name
    }
  )
}

# Dead Letter Queue for failed messages
resource "aws_sqs_queue" "chat_notifications_dlq" {
  count = var.enable_sqs ? 1 : 0

  name                       = "${local.sqs_queue_name}-dlq"
  message_retention_seconds  = 1209600 # 14 days
  sqs_managed_sse_enabled    = true

  tags = merge(
    local.common_tags,
    {
      Name = "${local.sqs_queue_name}-dlq"
    }
  )
}

# Configure DLQ redrive policy
resource "aws_sqs_queue_redrive_policy" "chat_notifications" {
  count     = var.enable_sqs ? 1 : 0
  queue_url = aws_sqs_queue.chat_notifications[0].id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.chat_notifications_dlq[0].arn
    maxReceiveCount     = 3
  })
}

###############################################################################
# SNS Topic for Push Notifications (Optional)
###############################################################################

resource "aws_sns_topic" "chat_alerts" {
  count = var.enable_sns ? 1 : 0

  name              = local.sns_topic_name
  display_name      = "Support Chat Alerts"
  kms_master_key_id = var.sns_kms_key_arn != "" ? var.sns_kms_key_arn : null

  tags = merge(
    local.common_tags,
    {
      Name = local.sns_topic_name
    }
  )
}

# SNS Topic Policy
resource "aws_sns_topic_policy" "chat_alerts" {
  count  = var.enable_sns ? 1 : 0
  arn    = aws_sns_topic.chat_alerts[0].arn
  policy = data.aws_iam_policy_document.sns_topic_policy[0].json
}

data "aws_iam_policy_document" "sns_topic_policy" {
  count = var.enable_sns ? 1 : 0

  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["s3.amazonaws.com"]
    }

    actions   = ["SNS:Publish"]
    resources = [aws_sns_topic.chat_alerts[0].arn]
  }
}

###############################################################################
# IAM User and Policies
###############################################################################

# IAM User for application
resource "aws_iam_user" "backend_user" {
  name = local.iam_user_name
  path = "/applications/"

  tags = merge(
    local.common_tags,
    {
      Name = local.iam_user_name
    }
  )
}

# IAM Policy for DynamoDB access
resource "aws_iam_policy" "dynamodb_access" {
  name        = "${local.resource_prefix}-dynamodb-access"
  description = "Policy for accessing chat messages DynamoDB table"
  policy      = data.aws_iam_policy_document.dynamodb_access.json
}

data "aws_iam_policy_document" "dynamodb_access" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:PutItem",
      "dynamodb:GetItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:DeleteItem",
      "dynamodb:UpdateItem",
      "dynamodb:DescribeTable",
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem"
    ]
    resources = [
      aws_dynamodb_table.chat_messages.arn,
      "${aws_dynamodb_table.chat_messages.arn}/index/*"
    ]
  }
}

# IAM Policy for S3 access
resource "aws_iam_policy" "s3_access" {
  name        = "${local.resource_prefix}-s3-access"
  description = "Policy for accessing chat attachments S3 bucket"
  policy      = data.aws_iam_policy_document.s3_access.json
}

data "aws_iam_policy_document" "s3_access" {
  statement {
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
      "s3:ListBucket",
      "s3:HeadBucket",
      "s3:GetObjectVersion"
    ]
    resources = [
      aws_s3_bucket.chat_attachments.arn,
      "${aws_s3_bucket.chat_attachments.arn}/*"
    ]
  }
}

# IAM Policy for SQS access (optional)
resource "aws_iam_policy" "sqs_access" {
  count       = var.enable_sqs ? 1 : 0
  name        = "${local.resource_prefix}-sqs-access"
  description = "Policy for accessing chat notifications SQS queue"
  policy      = data.aws_iam_policy_document.sqs_access[0].json
}

data "aws_iam_policy_document" "sqs_access" {
  count = var.enable_sqs ? 1 : 0

  statement {
    effect = "Allow"
    actions = [
      "sqs:SendMessage",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl"
    ]
    resources = [
      aws_sqs_queue.chat_notifications[0].arn
    ]
  }
}

# IAM Policy for SNS access (optional)
resource "aws_iam_policy" "sns_access" {
  count       = var.enable_sns ? 1 : 0
  name        = "${local.resource_prefix}-sns-access"
  description = "Policy for publishing to chat alerts SNS topic"
  policy      = data.aws_iam_policy_document.sns_access[0].json
}

data "aws_iam_policy_document" "sns_access" {
  count = var.enable_sns ? 1 : 0

  statement {
    effect = "Allow"
    actions = [
      "sns:Publish",
      "sns:Subscribe",
      "sns:Unsubscribe"
    ]
    resources = [
      aws_sns_topic.chat_alerts[0].arn
    ]
  }
}

# Attach policies to user
resource "aws_iam_user_policy_attachment" "dynamodb" {
  user       = aws_iam_user.backend_user.name
  policy_arn = aws_iam_policy.dynamodb_access.arn
}

resource "aws_iam_user_policy_attachment" "s3" {
  user       = aws_iam_user.backend_user.name
  policy_arn = aws_iam_policy.s3_access.arn
}

resource "aws_iam_user_policy_attachment" "sqs" {
  count      = var.enable_sqs ? 1 : 0
  user       = aws_iam_user.backend_user.name
  policy_arn = aws_iam_policy.sqs_access[0].arn
}

resource "aws_iam_user_policy_attachment" "sns" {
  count      = var.enable_sns ? 1 : 0
  user       = aws_iam_user.backend_user.name
  policy_arn = aws_iam_policy.sns_access[0].arn
}

# Create access key for the IAM user
resource "aws_iam_access_key" "backend_user" {
  user = aws_iam_user.backend_user.name
}

###############################################################################
# CloudWatch Alarms for Monitoring
###############################################################################

# DynamoDB Read Capacity Alarm
resource "aws_cloudwatch_metric_alarm" "dynamodb_read_throttle" {
  count               = var.enable_cloudwatch_alarms ? 1 : 0
  alarm_name          = "${local.dynamodb_table_name}-read-throttle"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ReadThrottleEvents"
  namespace           = "AWS/DynamoDB"
  period              = 300
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "Alert when DynamoDB read throttling occurs"
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = aws_dynamodb_table.chat_messages.name
  }

  alarm_actions = var.enable_sns ? [aws_sns_topic.chat_alerts[0].arn] : []
}

# DynamoDB Write Capacity Alarm
resource "aws_cloudwatch_metric_alarm" "dynamodb_write_throttle" {
  count               = var.enable_cloudwatch_alarms ? 1 : 0
  alarm_name          = "${local.dynamodb_table_name}-write-throttle"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "WriteThrottleEvents"
  namespace           = "AWS/DynamoDB"
  period              = 300
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "Alert when DynamoDB write throttling occurs"
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = aws_dynamodb_table.chat_messages.name
  }

  alarm_actions = var.enable_sns ? [aws_sns_topic.chat_alerts[0].arn] : []
}

# S3 Bucket Size Alarm
resource "aws_cloudwatch_metric_alarm" "s3_bucket_size" {
  count               = var.enable_cloudwatch_alarms ? 1 : 0
  alarm_name          = "${local.s3_bucket_name}-size"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "BucketSizeBytes"
  namespace           = "AWS/S3"
  period              = 86400 # 24 hours
  statistic           = "Average"
  threshold           = var.s3_size_alarm_threshold_gb * 1024 * 1024 * 1024
  alarm_description   = "Alert when S3 bucket size exceeds threshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    BucketName = aws_s3_bucket.chat_attachments.bucket
    StorageType = "StandardStorage"
  }

  alarm_actions = var.enable_sns ? [aws_sns_topic.chat_alerts[0].arn] : []
}
