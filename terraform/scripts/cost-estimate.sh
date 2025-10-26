#!/bin/bash

###############################################################################
# AWS Cost Estimation Script
#
# Provides estimated monthly costs for deployed infrastructure
#
# Usage:
#   ./scripts/cost-estimate.sh [environment]
###############################################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ENVIRONMENT="${1:-dev}"
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$TERRAFORM_DIR"

# Check if workspace exists
if ! terraform workspace select "$ENVIRONMENT" 2>/dev/null; then
    echo "Error: Environment '$ENVIRONMENT' not found."
    exit 1
fi

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}AWS Cost Estimation for: $ENVIRONMENT${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Get configuration
BILLING_MODE=$(terraform output -json deployment_summary 2>/dev/null | jq -r '.dynamodb_billing')
SQS_ENABLED=$(terraform output -json deployment_summary 2>/dev/null | jq -r '.sqs_enabled')
SNS_ENABLED=$(terraform output -json deployment_summary 2>/dev/null | jq -r '.sns_enabled')

echo -e "${GREEN}Configuration:${NC}"
echo "  DynamoDB Billing: $BILLING_MODE"
echo "  SQS Enabled: $SQS_ENABLED"
echo "  SNS Enabled: $SNS_ENABLED"
echo ""

# DynamoDB costs
echo -e "${BLUE}DynamoDB Costs:${NC}"
if [[ "$BILLING_MODE" == "PROVISIONED" ]]; then
    # Assuming 5 RCU/WCU for example
    READ_COST=$(echo "5 * 0.00013 * 730" | bc)
    WRITE_COST=$(echo "5 * 0.00065 * 730" | bc)
    DYNAMODB_TOTAL=$(echo "$READ_COST + $WRITE_COST" | bc)

    echo "  Read Capacity (5 RCU): \$${READ_COST}"
    echo "  Write Capacity (5 WCU): \$${WRITE_COST}"
    echo "  Subtotal: \$${DYNAMODB_TOTAL}/month"
elif [[ "$BILLING_MODE" == "PAY_PER_REQUEST" ]]; then
    echo "  On-Demand Pricing:"
    echo "    - Read Requests: \$0.25 per million"
    echo "    - Write Requests: \$1.25 per million"
    echo "  Estimated (10M reads, 2M writes): \$5.00/month"
    DYNAMODB_TOTAL=5.00
fi
echo ""

# S3 costs
echo -e "${BLUE}S3 Costs:${NC}"
echo "  Storage (Standard):"
echo "    - First 50 TB: \$0.023/GB"
echo "    - Estimated (20GB): \$0.46/month"
echo "  Requests:"
echo "    - PUT/POST: \$0.005 per 1,000"
echo "    - GET: \$0.0004 per 1,000"
echo "    - Estimated: \$0.50/month"
echo "  Data Transfer:"
echo "    - First 1 GB free"
echo "    - Then \$0.09/GB"
echo "  Subtotal: ~\$1.00/month"
S3_TOTAL=1.00
echo ""

# SQS costs (if enabled)
if [[ "$SQS_ENABLED" == "true" ]]; then
    echo -e "${BLUE}SQS Costs:${NC}"
    echo "  Requests:"
    echo "    - First 1M requests free"
    echo "    - Then \$0.40 per million"
    echo "  Estimated (5M requests): \$1.60/month"
    SQS_TOTAL=1.60
    echo ""
else
    SQS_TOTAL=0
fi

# SNS costs (if enabled)
if [[ "$SNS_ENABLED" == "true" ]]; then
    echo -e "${BLUE}SNS Costs:${NC}"
    echo "  Publishes:"
    echo "    - First 1M publishes free"
    echo "    - Then \$0.50 per million"
    echo "  Email Notifications:"
    echo "    - First 1,000 free"
    echo "    - Then \$2.00 per 100,000"
    echo "  Estimated (10K emails): \$0.50/month"
    SNS_TOTAL=0.50
    echo ""
else
    SNS_TOTAL=0
fi

# CloudWatch costs
echo -e "${BLUE}CloudWatch Costs:${NC}"
echo "  Metrics:"
echo "    - First 10 custom metrics free"
echo "    - Then \$0.30 per metric"
echo "  Alarms:"
echo "    - \$0.10 per alarm"
echo "  Estimated (3 alarms): \$0.30/month"
CLOUDWATCH_TOTAL=0.30
echo ""

# Calculate total
TOTAL=$(echo "$DYNAMODB_TOTAL + $S3_TOTAL + $SQS_TOTAL + $SNS_TOTAL + $CLOUDWATCH_TOTAL" | bc)

echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}Total Estimated Monthly Cost: \$${TOTAL}${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""

echo -e "${YELLOW}Notes:${NC}"
echo "  - Costs are estimates based on moderate usage"
echo "  - Actual costs vary with traffic and data volume"
echo "  - Free tier benefits not included (first 12 months)"
echo "  - Data transfer costs excluded"
echo "  - Enable cost allocation tags for accurate tracking"
echo ""

echo -e "${BLUE}Cost Optimization Tips:${NC}"
echo "  1. Use DynamoDB on-demand for dev/staging"
echo "  2. Enable S3 lifecycle policies to archive old files"
echo "  3. Use CloudWatch Logs Insights instead of exporting logs"
echo "  4. Set up billing alarms"
echo "  5. Review AWS Cost Explorer monthly"
echo ""
