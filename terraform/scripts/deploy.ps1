###############################################################################
# Terraform Deployment Script for Support Chat Infrastructure (Windows)
#
# Usage:
#   .\scripts\deploy.ps1 [environment] [action]
#
# Examples:
#   .\scripts\deploy.ps1 dev plan
#   .\scripts\deploy.ps1 staging apply
#   .\scripts\deploy.ps1 prod destroy
#
# Environments: dev, staging, prod
# Actions: init, plan, apply, destroy, output
###############################################################################

param(
    [Parameter(Position=0)]
    [ValidateSet('dev', 'staging', 'prod')]
    [string]$Environment = 'dev',

    [Parameter(Position=1)]
    [ValidateSet('init', 'plan', 'apply', 'destroy', 'output')]
    [string]$Action = 'plan'
)

# Stop on errors
$ErrorActionPreference = "Stop"

# Functions for colored output
function Write-Header {
    param([string]$Message)
    Write-Host "`n================================================" -ForegroundColor Blue
    Write-Host $Message -ForegroundColor Blue
    Write-Host "================================================`n" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

# Get script directory and terraform directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$TerraformDir = Split-Path -Parent $ScriptDir

# Change to terraform directory
Set-Location $TerraformDir

Write-Header "Terraform Deployment: $Environment - $Action"

# Check if Terraform is installed
try {
    $terraformVersion = terraform version
    Write-Success "Terraform is installed"
} catch {
    Write-Error-Custom "Terraform is not installed. Please install it first."
    Write-Host "`nInstall via Chocolatey: choco install terraform"
    Write-Host "Or download from: https://www.terraform.io/downloads"
    exit 1
}

# Check if AWS CLI is configured
try {
    $awsIdentity = aws sts get-caller-identity 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "AWS CLI not configured"
    }
    Write-Success "AWS CLI is configured"
} catch {
    Write-Error-Custom "AWS CLI is not configured. Please run 'aws configure' first."
    exit 1
}

# Show AWS account info
try {
    $accountId = (aws sts get-caller-identity --query Account --output text)
    $accountAlias = (aws iam list-account-aliases --query 'AccountAliases[0]' --output text 2>$null)
    if ([string]::IsNullOrEmpty($accountAlias)) {
        $accountAlias = "N/A"
    }
    Write-Success "AWS Account: $accountId ($accountAlias)"
} catch {
    Write-Warning-Custom "Could not retrieve AWS account information"
}

# Initialize Terraform (always run for safety)
if ($Action -eq 'init' -or !(Test-Path ".terraform")) {
    Write-Header "Initializing Terraform"
    terraform init
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Terraform initialization failed"
        exit 1
    }
    Write-Success "Terraform initialized"
}

# Select or create workspace
Write-Header "Selecting Workspace: $Environment"
$workspaceExists = terraform workspace list | Select-String -Pattern "^\s*\*?\s*$Environment\s*$"
if ($workspaceExists) {
    terraform workspace select $Environment
} else {
    terraform workspace new $Environment
}
if ($LASTEXITCODE -ne 0) {
    Write-Error-Custom "Failed to select/create workspace"
    exit 1
}
$currentWorkspace = terraform workspace show
Write-Success "Workspace: $currentWorkspace"

# Set variables file
$varFile = "environments\${Environment}.tfvars"

if (!(Test-Path $varFile)) {
    Write-Error-Custom "Variables file not found: $varFile"
    exit 1
}

# Execute action
switch ($Action) {
    'plan' {
        Write-Header "Planning Infrastructure Changes"
        terraform plan -var-file="$varFile" -out="${Environment}.tfplan"
        if ($LASTEXITCODE -ne 0) {
            Write-Error-Custom "Terraform plan failed"
            exit 1
        }
        Write-Success "Plan saved to ${Environment}.tfplan"
        Write-Host ""
        Write-Warning-Custom "Review the plan above before applying."
        Write-Host "To apply: .\scripts\deploy.ps1 $Environment apply"
    }

    'apply' {
        Write-Header "Applying Infrastructure Changes"

        # Check if plan file exists
        if (Test-Path "${Environment}.tfplan") {
            Write-Warning-Custom "Applying from saved plan: ${Environment}.tfplan"
            terraform apply "${Environment}.tfplan"
            if ($LASTEXITCODE -eq 0) {
                Remove-Item "${Environment}.tfplan" -ErrorAction SilentlyContinue
            }
        } else {
            Write-Warning-Custom "No saved plan found. Running plan and apply..."
            terraform apply -var-file="$varFile"
        }

        if ($LASTEXITCODE -ne 0) {
            Write-Error-Custom "Terraform apply failed"
            exit 1
        }

        Write-Success "Infrastructure deployed successfully!"
        Write-Host ""
        Write-Header "Deployment Outputs"
        terraform output deployment_summary
        Write-Host ""
        Write-Warning-Custom "IMPORTANT: Save the access credentials securely!"
        Write-Host "Run this command to view credentials:"
        Write-Host "  terraform output -raw application_env_vars"
    }

    'destroy' {
        Write-Header "DESTROYING Infrastructure"
        Write-Warning-Custom "⚠️  WARNING: This will DELETE all resources in $Environment!"
        Write-Warning-Custom "⚠️  This action CANNOT be undone!"
        Write-Host ""

        $confirmation = Read-Host "Are you sure you want to destroy $Environment? (type 'yes' to confirm)"

        if ($confirmation -ne 'yes') {
            Write-Warning-Custom "Destroy cancelled."
            exit 0
        }

        Write-Warning-Custom "Starting destruction in 5 seconds... (Ctrl+C to cancel)"
        Start-Sleep -Seconds 5

        terraform destroy -var-file="$varFile"
        if ($LASTEXITCODE -ne 0) {
            Write-Error-Custom "Terraform destroy failed"
            exit 1
        }
        Write-Success "Infrastructure destroyed."
    }

    'output' {
        Write-Header "Terraform Outputs"
        terraform output
    }
}

Write-Success "Operation completed successfully!"
