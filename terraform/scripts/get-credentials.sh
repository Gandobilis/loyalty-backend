#!/bin/bash

###############################################################################
# Script to retrieve AWS credentials from Terraform outputs
#
# Usage:
#   ./scripts/get-credentials.sh [environment] [format]
#
# Examples:
#   ./scripts/get-credentials.sh dev env
#   ./scripts/get-credentials.sh prod json
#   ./scripts/get-credentials.sh staging plain
#
# Formats:
#   env   - .env file format (default)
#   json  - JSON format
#   plain - Plain text
###############################################################################

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

ENVIRONMENT="${1:-dev}"
FORMAT="${2:-env}"
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$TERRAFORM_DIR"

# Check if workspace exists
if ! terraform workspace select "$ENVIRONMENT" 2>/dev/null; then
    echo "Error: Environment '$ENVIRONMENT' not found."
    exit 1
fi

# Get outputs
REGION=$(terraform output -raw aws_region 2>/dev/null || echo "")
ACCESS_KEY=$(terraform output -raw iam_access_key_id 2>/dev/null || echo "")
SECRET_KEY=$(terraform output -raw iam_secret_access_key 2>/dev/null || echo "")
DYNAMODB_TABLE=$(terraform output -raw dynamodb_table_name 2>/dev/null || echo "")
S3_BUCKET=$(terraform output -raw s3_bucket_name 2>/dev/null || echo "")
SQS_URL=$(terraform output -raw sqs_queue_url 2>/dev/null || echo "")
SNS_ARN=$(terraform output -raw sns_topic_arn 2>/dev/null || echo "")

# Check if credentials exist
if [[ -z "$ACCESS_KEY" ]] || [[ -z "$SECRET_KEY" ]]; then
    echo "Error: Credentials not found. Has infrastructure been deployed?"
    exit 1
fi

# Output based on format
case "$FORMAT" in
    env)
        echo -e "${GREEN}# AWS Credentials for $ENVIRONMENT${NC}"
        echo "AWS_REGION=$REGION"
        echo "AWS_ACCESS_KEY_ID=$ACCESS_KEY"
        echo "AWS_SECRET_ACCESS_KEY=$SECRET_KEY"
        echo ""
        echo "# DynamoDB Configuration"
        echo "AWS_DYNAMODB_CHAT_TABLE=$DYNAMODB_TABLE"
        echo ""
        echo "# S3 Configuration"
        echo "AWS_S3_BUCKET_NAME=$S3_BUCKET"
        echo "AWS_S3_PRESIGNED_URL_DURATION=3600"

        if [[ -n "$SQS_URL" ]] && [[ "$SQS_URL" != "null" ]]; then
            echo ""
            echo "# SQS Configuration"
            echo "AWS_SQS_QUEUE_URL=$SQS_URL"
        fi

        if [[ -n "$SNS_ARN" ]] && [[ "$SNS_ARN" != "null" ]]; then
            echo ""
            echo "# SNS Configuration"
            echo "AWS_SNS_TOPIC_ARN=$SNS_ARN"
        fi
        ;;

    json)
        cat <<EOF
{
  "aws_region": "$REGION",
  "aws_access_key_id": "$ACCESS_KEY",
  "aws_secret_access_key": "$SECRET_KEY",
  "dynamodb_table": "$DYNAMODB_TABLE",
  "s3_bucket": "$S3_BUCKET",
  "sqs_queue_url": "$SQS_URL",
  "sns_topic_arn": "$SNS_ARN"
}
EOF
        ;;

    plain)
        echo "AWS Region: $REGION"
        echo "Access Key ID: $ACCESS_KEY"
        echo "Secret Access Key: $SECRET_KEY"
        echo "DynamoDB Table: $DYNAMODB_TABLE"
        echo "S3 Bucket: $S3_BUCKET"

        if [[ -n "$SQS_URL" ]] && [[ "$SQS_URL" != "null" ]]; then
            echo "SQS Queue URL: $SQS_URL"
        fi

        if [[ -n "$SNS_ARN" ]] && [[ "$SNS_ARN" != "null" ]]; then
            echo "SNS Topic ARN: $SNS_ARN"
        fi
        ;;

    *)
        echo "Error: Invalid format '$FORMAT'. Must be env, json, or plain."
        exit 1
        ;;
esac

echo ""
echo -e "${YELLOW}⚠️  Keep these credentials secure! Do not commit to version control.${NC}"
