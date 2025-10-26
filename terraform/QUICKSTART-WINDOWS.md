# Windows Quick Start - Terraform Deployment

This is a simplified guide to get your infrastructure running on Windows in minutes.

## Prerequisites Checklist

- [ ] Windows 10/11
- [ ] PowerShell 5.1 or later
- [ ] Terraform installed
- [ ] AWS CLI installed and configured
- [ ] AWS account with appropriate permissions

## Step-by-Step Instructions

### 1. Install Required Tools

Open PowerShell as Administrator:

```powershell
# Install Chocolatey (if not already installed)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Terraform and AWS CLI
choco install terraform awscli -y
```

Close and reopen PowerShell to refresh environment variables.

### 2. Configure AWS

```powershell
aws configure
```

Enter your:
- AWS Access Key ID
- AWS Secret Access Key
- Default region (e.g., `us-east-1`)
- Default output format (use `json`)

Verify it works:
```powershell
aws sts get-caller-identity
```

### 3. Enable PowerShell Scripts

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 4. Navigate to Terraform Directory

```powershell
cd C:\Users\Sandro\Documents\projects\loyalty-backend\terraform
```

### 5. Review Configuration

Open the development configuration file to verify settings:

```powershell
notepad environments\dev.tfvars
```

Key settings to check:
- `aws_region` - Should match your AWS region
- `cors_allowed_origins` - Add your frontend URLs

### 6. Deploy Infrastructure

**Option A: Using Batch File (Easiest)**
```cmd
deploy-dev.bat plan
```

Review the plan, then apply:
```cmd
deploy-dev.bat apply
```

**Option B: Using PowerShell Script**
```powershell
.\scripts\deploy.ps1 dev plan
.\scripts\deploy.ps1 dev apply
```

**Option C: Using Terraform Directly**
```powershell
terraform init
terraform workspace new dev
terraform plan -var-file="environments\dev.tfvars"
terraform apply -var-file="environments\dev.tfvars"
```

### 7. Get Your Credentials

```powershell
.\scripts\get-credentials.ps1 dev
```

This will:
- Display your AWS credentials
- Offer to save them to your `.env` file

### 8. Update Your Application

Copy the credentials to your application's `.env` file:

```
C:\Users\Sandro\Documents\projects\loyalty-backend\.env
```

The credentials will look like:
```env
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
DYNAMODB_TABLE_NAME=loyalty-backend-dev-chat-messages
S3_BUCKET_NAME=loyalty-backend-dev-chat-attachments-...
```

### 9. Verify Resources

Check AWS Console:
1. Go to https://console.aws.amazon.com
2. Navigate to DynamoDB → Tables
3. Navigate to S3 → Buckets
4. Verify your resources were created

### 10. Test Your Application

Start your application and test the chat functionality:
- Send messages
- Upload attachments
- Verify data in DynamoDB and S3

## Quick Reference Commands

### View What Will Be Created
```powershell
.\scripts\deploy.ps1 dev plan
```

### Deploy Infrastructure
```powershell
.\scripts\deploy.ps1 dev apply
```

### Get Credentials
```powershell
.\scripts\get-credentials.ps1 dev
```

### View Outputs
```powershell
terraform output
```

### View Sensitive Credentials
```powershell
terraform output -raw iam_access_key_id
terraform output -raw iam_secret_access_key
```

### Destroy Everything (Be Careful!)
```powershell
.\scripts\deploy.ps1 dev destroy
```

## Different Environments

### Deploy to Staging
```powershell
.\scripts\deploy.ps1 staging plan
.\scripts\deploy.ps1 staging apply
```

### Deploy to Production
```powershell
.\scripts\deploy.ps1 prod plan
.\scripts\deploy.ps1 prod apply
```

## Common Issues and Solutions

### Issue: "Terraform not found"
**Solution:** Restart PowerShell after installation

### Issue: "AWS CLI not configured"
**Solution:** Run `aws configure` with your credentials

### Issue: "Access Denied"
**Solution:** Check your AWS credentials and IAM permissions

### Issue: "S3 bucket name already taken"
**Solution:** Edit `environments\dev.tfvars` and set a unique bucket name:
```hcl
s3_bucket_name = "my-unique-name-12345"
```

### Issue: "Cannot run scripts"
**Solution:** Enable script execution:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## Cost Estimation

Development environment (default settings):
- DynamoDB (PAY_PER_REQUEST): ~$1-5/month for light usage
- S3: ~$0.50-2/month for storage
- CloudWatch: ~$0.50/month
- IAM: Free

**Estimated Monthly Cost: $2-10** (depending on usage)

## What Gets Created

### Core Resources
1. **DynamoDB Table** - Stores chat messages
2. **S3 Bucket** - Stores file attachments
3. **IAM User** - For application access
4. **IAM Policies** - Permissions for DynamoDB and S3
5. **CloudWatch Alarms** - Monitoring and alerts

### Optional Resources (disabled by default in dev)
- SQS Queue (for notifications)
- SNS Topic (for alerts)
- Enhanced monitoring

## Security Best Practices

1. **Never commit these files:**
   - `terraform.tfvars`
   - `*.tfstate`
   - `.env`

2. **Protect credentials:**
   - Store in password manager
   - Don't share in emails/chat
   - Rotate regularly

3. **Enable MFA:**
   - Enable on AWS root account
   - Enable on IAM users

4. **Review permissions:**
   - Use least privilege principle
   - Audit access regularly

## Next Steps

After successful deployment:

1. [ ] Verify resources in AWS Console
2. [ ] Copy credentials to `.env` file
3. [ ] Update application configuration
4. [ ] Test chat functionality
5. [ ] Set up monitoring alerts
6. [ ] Review CloudWatch logs
7. [ ] Plan for production deployment

## Getting Help

- **Detailed Guide:** See `WINDOWS-GUIDE.md`
- **General Info:** See `README.md`
- **Terraform Docs:** https://www.terraform.io/docs
- **AWS Docs:** https://docs.aws.amazon.com

## Troubleshooting

Enable debug logging:
```powershell
$env:TF_LOG="DEBUG"
.\scripts\deploy.ps1 dev plan
```

Check Terraform state:
```powershell
terraform state list
terraform show
```

Verify AWS access:
```powershell
aws sts get-caller-identity
aws iam get-user
```

## Clean Up

To remove all resources (this will delete all data!):
```powershell
.\scripts\deploy.ps1 dev destroy
```

---

**Ready to deploy? Start with Step 1 above!**
