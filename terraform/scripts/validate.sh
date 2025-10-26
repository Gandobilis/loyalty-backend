#!/bin/bash

###############################################################################
# Terraform Validation Script
#
# Validates Terraform configuration and checks for common issues
#
# Usage:
#   ./scripts/validate.sh [environment]
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ENVIRONMENT="${1:-dev}"
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$TERRAFORM_DIR"

print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
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

ERRORS=0

# Check Terraform installation
print_header "Checking Terraform Installation"
if command -v terraform &> /dev/null; then
    VERSION=$(terraform version -json | jq -r '.terraform_version')
    print_success "Terraform $VERSION installed"
else
    print_error "Terraform not installed"
    ((ERRORS++))
fi

# Check AWS CLI
print_header "Checking AWS CLI"
if command -v aws &> /dev/null; then
    AWS_VERSION=$(aws --version | cut -d' ' -f1)
    print_success "$AWS_VERSION installed"

    if aws sts get-caller-identity &> /dev/null; then
        ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
        USER=$(aws sts get-caller-identity --query Arn --output text)
        print_success "AWS authenticated as: $USER"
        print_success "AWS Account: $ACCOUNT"
    else
        print_error "AWS CLI not configured"
        ((ERRORS++))
    fi
else
    print_error "AWS CLI not installed"
    ((ERRORS++))
fi

# Validate Terraform syntax
print_header "Validating Terraform Syntax"
if terraform fmt -check -recursive; then
    print_success "Terraform files are properly formatted"
else
    print_warning "Terraform files need formatting (run: terraform fmt -recursive)"
fi

if terraform validate; then
    print_success "Terraform configuration is valid"
else
    print_error "Terraform configuration has errors"
    ((ERRORS++))
fi

# Check for required files
print_header "Checking Required Files"
REQUIRED_FILES=(
    "main.tf"
    "variables.tf"
    "outputs.tf"
    "environments/${ENVIRONMENT}.tfvars"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        print_success "Found: $file"
    else
        print_error "Missing: $file"
        ((ERRORS++))
    fi
done

# Check for sensitive files in git
print_header "Checking Git Safety"
if [[ -d ".git" ]] || git rev-parse --git-dir > /dev/null 2>&1; then
    # Check if .gitignore exists
    if [[ -f ".gitignore" ]]; then
        print_success ".gitignore exists"

        # Check if tfvars are ignored
        if grep -q "*.tfvars" .gitignore; then
            print_success "*.tfvars is in .gitignore"
        else
            print_warning "*.tfvars should be in .gitignore"
        fi

        # Check if tfstate are ignored
        if grep -q "*.tfstate" .gitignore; then
            print_success "*.tfstate is in .gitignore"
        else
            print_error "*.tfstate MUST be in .gitignore"
            ((ERRORS++))
        fi
    else
        print_warning ".gitignore not found"
    fi

    # Check if sensitive files are tracked
    if git ls-files | grep -E '\.tfvars$|\.tfstate$' > /dev/null; then
        print_error "Sensitive files are tracked in Git!"
        git ls-files | grep -E '\.tfvars$|\.tfstate$'
        ((ERRORS++))
    else
        print_success "No sensitive files tracked in Git"
    fi
fi

# Check Terraform state
print_header "Checking Terraform State"
if [[ -d ".terraform" ]]; then
    print_success "Terraform initialized"

    # Check workspace
    CURRENT_WORKSPACE=$(terraform workspace show 2>/dev/null || echo "none")
    if [[ "$CURRENT_WORKSPACE" != "default" ]]; then
        print_success "Using workspace: $CURRENT_WORKSPACE"
    else
        print_warning "Using default workspace (consider using named workspaces)"
    fi
else
    print_warning "Terraform not initialized (run: terraform init)"
fi

# Check AWS permissions
print_header "Checking AWS Permissions"
REQUIRED_ACTIONS=(
    "dynamodb:CreateTable"
    "s3:CreateBucket"
    "iam:CreateUser"
    "iam:CreatePolicy"
)

echo "Simulating required AWS actions..."
for action in "${REQUIRED_ACTIONS[@]}"; do
    SERVICE=$(echo "$action" | cut -d: -f1)
    # Basic check - actual permission check would require policy simulation
    echo "  - $action (manual verification recommended)"
done
print_warning "Note: Actual permission check requires manual verification or policy simulation"

# Summary
echo ""
print_header "Validation Summary"
if [[ $ERRORS -eq 0 ]]; then
    print_success "All checks passed! Ready to deploy."
    exit 0
else
    print_error "Found $ERRORS error(s). Please fix before deploying."
    exit 1
fi
