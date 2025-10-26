# Windows Setup Guide for Terraform

This guide provides Windows-specific instructions for running the Terraform infrastructure for your Loyalty Backend support chat system.

## Prerequisites for Windows

### 1. Install Terraform

**Option A: Using Chocolatey (Recommended)**
```powershell
# Install Chocolatey if not already installed
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Terraform
choco install terraform -y
```

**Option B: Manual Installation**
1. Download from: https://www.terraform.io/downloads
2. Extract the `terraform.exe` file
3. Add to your PATH:
   - Right-click "This PC" → Properties → Advanced system settings
   - Click "Environment Variables"
   - Edit "Path" variable and add the folder containing `terraform.exe`

**Verify Installation:**
```powershell
terraform version
```

### 2. Install AWS CLI

**Option A: Using Chocolatey**
```powershell
choco install awscli -y
```

**Option B: MSI Installer**
Download from: https://aws.amazon.com/cli/

**Verify Installation:**
```powershell
aws --version
```

### 3. Configure AWS CLI

Run the configuration wizard:
```powershell
aws configure
```

You'll need to provide:
- AWS Access Key ID
- AWS Secret Access Key
- Default region (e.g., `us-east-1`)
- Default output format (e.g., `json`)

**Verify Configuration:**
```powershell
aws sts get-caller-identity
```

### 4. Enable PowerShell Script Execution

Open PowerShell as Administrator and run:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## Quick Start on Windows

### Step 1: Navigate to Terraform Directory

```powershell
cd C:\Users\Sandro\Documents\projects\loyalty-backend\terraform
```

### Step 2: Initialize Terraform

```powershell
terraform init
```

### Step 3: Create Variables File

```powershell
# Copy the example file
Copy-Item terraform.tfvars.example terraform.tfvars

# Edit with Notepad or your preferred editor
notepad terraform.tfvars
```

Update the following values in `terraform.tfvars`:
- `aws_region`: Your preferred AWS region
- `cors_allowed_origins`: Your frontend URLs
- Other settings as needed

### Step 4: Plan Infrastructure

**Using PowerShell Script (Recommended):**
```powershell
.\scripts\deploy.ps1 dev plan
```

**Using Terraform Directly:**
```powershell
terraform workspace new dev
terraform workspace select dev
terraform plan -var-file="environments\dev.tfvars"
```

### Step 5: Apply Infrastructure

**Using PowerShell Script:**
```powershell
.\scripts\deploy.ps1 dev apply
```

**Using Terraform Directly:**
```powershell
terraform apply -var-file="environments\dev.tfvars"
```

### Step 6: Get Credentials

**Using PowerShell Script:**
```powershell
.\scripts\get-credentials.ps1 dev
```

**Using Terraform Directly:**
```powershell
terraform output -raw application_env_vars
```

Copy the output to your `.env` file in the project root.

## PowerShell Scripts

### Deploy Script

**Location:** `scripts\deploy.ps1`

**Usage:**
```powershell
.\scripts\deploy.ps1 [environment] [action]
```

**Examples:**
```powershell
# Plan changes for dev environment
.\scripts\deploy.ps1 dev plan

# Apply changes for staging
.\scripts\deploy.ps1 staging apply

# View outputs
.\scripts\deploy.ps1 dev output

# Destroy infrastructure
.\scripts\deploy.ps1 dev destroy
```

### Get Credentials Script

**Location:** `scripts\get-credentials.ps1`

**Usage:**
```powershell
.\scripts\get-credentials.ps1 [environment]
```

**Examples:**
```powershell
# Get dev credentials
.\scripts\get-credentials.ps1 dev

# Get production credentials
.\scripts\get-credentials.ps1 prod
```

## Environment-Specific Deployments

### Development Environment

```powershell
# Create and select workspace
terraform workspace new dev
terraform workspace select dev

# Plan
terraform plan -var-file="environments\dev.tfvars"

# Apply
terraform apply -var-file="environments\dev.tfvars"
```

### Staging Environment

```powershell
terraform workspace new staging
terraform workspace select staging
terraform plan -var-file="environments\staging.tfvars"
terraform apply -var-file="environments\staging.tfvars"
```

### Production Environment

```powershell
terraform workspace new prod
terraform workspace select prod
terraform plan -var-file="environments\prod.tfvars"
terraform apply -var-file="environments\prod.tfvars"
```

## Common Windows Commands

### View Terraform State

```powershell
# List all resources
terraform state list

# Show specific resource
terraform state show aws_dynamodb_table.chat_messages

# Show all state
terraform show
```

### Format Code

```powershell
# Format all .tf files
terraform fmt -recursive
```

### View Outputs

```powershell
# View all outputs
terraform output

# View specific output
terraform output dynamodb_table_name

# View sensitive output
terraform output -raw iam_secret_access_key
```

### Validate Configuration

```powershell
terraform validate
```

## Troubleshooting Windows-Specific Issues

### PowerShell Execution Policy Error

**Error:** "cannot be loaded because running scripts is disabled"

**Solution:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Path Issues with Spaces

If your path contains spaces, use quotes:
```powershell
cd "C:\Users\Sandro\Documents\projects\loyalty-backend\terraform"
```

### AWS CLI Not Found

**Solution:**
1. Restart PowerShell after installation
2. Check PATH variable includes AWS CLI location
3. Reinstall AWS CLI

### Terraform Not Found

**Solution:**
1. Verify installation: `terraform version`
2. Check PATH variable
3. Restart PowerShell

### Permission Denied Errors

**Solution:**
1. Run PowerShell as Administrator
2. Check AWS credentials: `aws sts get-caller-identity`
3. Verify IAM permissions

## File Paths on Windows

When editing Terraform files, use Windows path format:

- Correct: `C:\Users\Sandro\Documents\projects\loyalty-backend\terraform`
- Correct in Terraform: Use forward slashes `/` or escape backslashes `\\`

## Using Git Bash as Alternative

If you prefer using Git Bash (comes with Git for Windows):

```bash
# Navigate to terraform directory
cd /c/Users/Sandro/Documents/projects/loyalty-backend/terraform

# Run the bash scripts directly
./scripts/deploy.sh dev plan
./scripts/deploy.sh dev apply
./scripts/get-credentials.sh dev
```

## Saving Credentials to .env File

### Option 1: Using PowerShell Script (Automated)

```powershell
.\scripts\get-credentials.ps1 dev
# Follow the prompts to save to .env file
```

### Option 2: Manual Copy

```powershell
# Get credentials
terraform output -raw application_env_vars

# Copy output and append to .env file in project root
# Example: C:\Users\Sandro\Documents\projects\loyalty-backend\.env
```

### Option 3: Direct Append

```powershell
terraform output -raw application_env_vars | Out-File -Append -FilePath ..\.env
```

## Next Steps

After successfully deploying the infrastructure:

1. **Verify Resources in AWS Console:**
   - DynamoDB Tables
   - S3 Buckets
   - IAM Users

2. **Update Application Configuration:**
   - Add credentials to `.env` file
   - Update `application.properties` or equivalent
   - Restart your application

3. **Test the Setup:**
   - Test DynamoDB connection
   - Test S3 upload/download
   - Verify chat functionality

4. **Set Up Monitoring:**
   - Check CloudWatch dashboards
   - Configure alerts
   - Review logs

## Security Reminders

- Never commit `terraform.tfvars` or `terraform.tfstate` files
- Store credentials securely (use password manager)
- Rotate AWS credentials regularly
- Enable MFA on AWS account
- Review IAM permissions regularly

## Additional Resources

- [Terraform Windows Documentation](https://learn.hashicorp.com/tutorials/terraform/install-cli)
- [AWS CLI Windows Installation](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-windows.html)
- [PowerShell Documentation](https://docs.microsoft.com/en-us/powershell/)

## Getting Help

If you encounter issues:

1. Check Terraform logs:
   ```powershell
   $env:TF_LOG="DEBUG"
   terraform apply -var-file="environments\dev.tfvars"
   ```

2. Verify AWS permissions:
   ```powershell
   aws sts get-caller-identity
   aws iam get-user
   ```

3. Check Terraform state:
   ```powershell
   terraform show
   terraform state list
   ```

4. Review the main README.md for general troubleshooting
