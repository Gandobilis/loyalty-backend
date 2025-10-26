#!/bin/bash

###############################################################################
# Terraform Deployment Script for Support Chat Infrastructure
#
# Usage:
#   ./scripts/deploy.sh [environment] [action]
#
# Examples:
#   ./scripts/deploy.sh dev plan
#   ./scripts/deploy.sh staging apply
#   ./scripts/deploy.sh prod destroy
#
# Environments: dev, staging, prod
# Actions: init, plan, apply, destroy, output
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="${1:-dev}"
ACTION="${2:-plan}"
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|prod)$ ]]; then
    echo -e "${RED}Error: Invalid environment '$ENVIRONMENT'. Must be dev, staging, or prod.${NC}"
    exit 1
fi

# Validate action
if [[ ! "$ACTION" =~ ^(init|plan|apply|destroy|output)$ ]]; then
    echo -e "${RED}Error: Invalid action '$ACTION'. Must be init, plan, apply, destroy, or output.${NC}"
    exit 1
fi

# Functions
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Change to terraform directory
cd "$TERRAFORM_DIR"

print_header "Terraform Deployment: $ENVIRONMENT - $ACTION"

# Check if Terraform is installed
if ! command -v terraform &> /dev/null; then
    print_error "Terraform is not installed. Please install it first."
    exit 1
fi

# Check if AWS CLI is configured
if ! aws sts get-caller-identity &> /dev/null; then
    print_error "AWS CLI is not configured. Please run 'aws configure' first."
    exit 1
fi

# Show AWS account info
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ACCOUNT_ALIAS=$(aws iam list-account-aliases --query 'AccountAliases[0]' --output text 2>/dev/null || echo "N/A")
print_success "AWS Account: $ACCOUNT_ID ($ACCOUNT_ALIAS)"

# Initialize Terraform (always run for safety)
if [[ "$ACTION" == "init" ]] || [[ ! -d ".terraform" ]]; then
    print_header "Initializing Terraform"
    terraform init
    print_success "Terraform initialized"
fi

# Select or create workspace
print_header "Selecting Workspace: $ENVIRONMENT"
terraform workspace select "$ENVIRONMENT" 2>/dev/null || terraform workspace new "$ENVIRONMENT"
print_success "Workspace: $(terraform workspace show)"

# Set variables file
VAR_FILE="environments/${ENVIRONMENT}.tfvars"

if [[ ! -f "$VAR_FILE" ]]; then
    print_error "Variables file not found: $VAR_FILE"
    exit 1
fi

# Execute action
case "$ACTION" in
    plan)
        print_header "Planning Infrastructure Changes"
        terraform plan -var-file="$VAR_FILE" -out="${ENVIRONMENT}.tfplan"
        print_success "Plan saved to ${ENVIRONMENT}.tfplan"
        echo ""
        print_warning "Review the plan above before applying."
        echo "To apply: ./scripts/deploy.sh $ENVIRONMENT apply"
        ;;

    apply)
        print_header "Applying Infrastructure Changes"

        # Check if plan file exists
        if [[ -f "${ENVIRONMENT}.tfplan" ]]; then
            print_warning "Applying from saved plan: ${ENVIRONMENT}.tfplan"
            terraform apply "${ENVIRONMENT}.tfplan"
            rm -f "${ENVIRONMENT}.tfplan"
        else
            print_warning "No saved plan found. Running plan and apply..."
            terraform apply -var-file="$VAR_FILE"
        fi

        print_success "Infrastructure deployed successfully!"
        echo ""
        print_header "Deployment Outputs"
        terraform output deployment_summary
        echo ""
        print_warning "IMPORTANT: Save the access credentials securely!"
        echo "Run this command to view credentials:"
        echo "  terraform output -raw application_env_vars"
        ;;

    destroy)
        print_header "DESTROYING Infrastructure"
        print_warning "⚠️  WARNING: This will DELETE all resources in $ENVIRONMENT!"
        print_warning "⚠️  This action CANNOT be undone!"
        echo ""

        read -p "Are you sure you want to destroy $ENVIRONMENT? (type 'yes' to confirm): " CONFIRM

        if [[ "$CONFIRM" != "yes" ]]; then
            print_warning "Destroy cancelled."
            exit 0
        fi

        print_warning "Starting destruction in 5 seconds... (Ctrl+C to cancel)"
        sleep 5

        terraform destroy -var-file="$VAR_FILE"
        print_success "Infrastructure destroyed."
        ;;

    output)
        print_header "Terraform Outputs"
        terraform output
        ;;
esac

print_success "Operation completed successfully!"
