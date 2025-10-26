# Terraform Quick Start Guide

Complete guide to deploy AWS infrastructure for support chat using Terraform in 5 minutes.

## 🚀 Quick Deploy (Development)

```bash
# 1. Navigate to terraform directory
cd terraform

# 2. Make scripts executable (Unix/Mac)
chmod +x scripts/*.sh

# 3. Initialize Terraform
terraform init

# 4. Validate configuration
./scripts/validate.sh dev

# 5. Deploy to development
./scripts/deploy.sh dev apply

# 6. Get credentials
./scripts/get-credentials.sh dev env >> ../.env

# Done! Your infrastructure is ready.
```

## 📋 Prerequisites

### Install Required Tools

**Terraform** (Version >= 1.0):
```bash
# macOS
brew install terraform

# Windows (with Chocolatey)
choco install terraform

# Linux
wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
unzip terraform_1.6.0_linux_amd64.zip
sudo mv terraform /usr/local/bin/
```

**AWS CLI**:
```bash
# macOS
brew install awscli

# Windows
choco install awscli

# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

**Configure AWS**:
```bash
aws configure
# Enter your AWS Access Key ID
# Enter your AWS Secret Access Key
# Default region: us-east-1
# Default output format: json
```

## 🎯 Step-by-Step Guide

### Step 1: Review Configuration

Check the environment configuration for your target environment:

```bash
# View development config
cat terraform/environments/dev.tfvars

# View production config
cat terraform/environments/prod.tfvars
```

### Step 2: Initialize Terraform

```bash
cd terraform
terraform init
```

You should see:
```
Terraform has been successfully initialized!
```

### Step 3: Validate Configuration

```bash
./scripts/validate.sh dev
```

All checks should pass ✓

### Step 4: Preview Changes

```bash
./scripts/deploy.sh dev plan
```

Review the output to see what will be created:
- DynamoDB table
- S3 bucket
- IAM user and policies
- (Optional) SQS queue
- (Optional) SNS topic
- CloudWatch alarms

### Step 5: Deploy Infrastructure

```bash
./scripts/deploy.sh dev apply
```

Type `yes` when prompted.

Deployment takes ~2-3 minutes.

### Step 6: Get Credentials

```bash
# View credentials in terminal
./scripts/get-credentials.sh dev env

# Or save directly to .env file
./scripts/get-credentials.sh dev env >> ../.env
```

### Step 7: Verify Deployment

```bash
# Check deployment summary
terraform output deployment_summary

# Verify DynamoDB table
aws dynamodb describe-table --table-name $(terraform output -raw dynamodb_table_name)

# Verify S3 bucket
aws s3 ls $(terraform output -raw s3_bucket_name)
```

## 🌍 Deploy to Different Environments

### Development
```bash
./scripts/deploy.sh dev apply
```

### Staging
```bash
./scripts/deploy.sh staging apply
```

### Production
```bash
./scripts/deploy.sh prod apply
```

## 📦 What Gets Created

### Development Environment

```
AWS Resources Created:
├── DynamoDB Table
│   ├── Name: loyalty-backend-dev-chat-messages
│   ├── Billing: PAY_PER_REQUEST (on-demand)
│   ├── Encryption: AES256
│   └── Backup: Disabled (cost savings)
│
├── S3 Bucket
│   ├── Name: loyalty-backend-dev-chat-attachments-{account-id}
│   ├── Encryption: AES256
│   ├── Versioning: Disabled
│   └── Lifecycle: Disabled
│
├── IAM User
│   ├── Name: loyalty-backend-dev-backend-user
│   ├── Access Key: Auto-generated
│   └── Policies: DynamoDB + S3 access
│
└── CloudWatch Alarms (3)
    ├── DynamoDB Read Throttle
    ├── DynamoDB Write Throttle
    └── S3 Bucket Size

Estimated Monthly Cost: $10-15
```

### Production Environment

```
AWS Resources Created:
├── DynamoDB Table
│   ├── Name: loyalty-backend-prod-chat-messages
│   ├── Billing: PROVISIONED with auto-scaling
│   ├── Capacity: 10 RCU/WCU (scales to 200)
│   ├── Encryption: AES256 (or KMS)
│   ├── Backup: Point-in-Time Recovery enabled
│   └── Streams: Enabled for real-time processing
│
├── S3 Bucket
│   ├── Name: loyalty-backend-prod-chat-attachments-{account-id}
│   ├── Encryption: AES256 or KMS
│   ├── Versioning: Enabled
│   ├── Lifecycle: Archive to Glacier after 90 days
│   └── Logging: Enabled
│
├── SQS Queue
│   ├── Name: loyalty-backend-prod-chat-notifications
│   ├── DLQ: Included
│   └── Encryption: Enabled
│
├── SNS Topic
│   ├── Name: loyalty-backend-prod-chat-alerts
│   └── Subscriptions: Email alerts
│
├── IAM User
│   ├── Name: loyalty-backend-prod-backend-user
│   ├── Access Key: Auto-generated
│   └── Policies: Full access to all resources
│
└── CloudWatch Alarms (3+)
    └── Monitoring all resources

Estimated Monthly Cost: $30-50
```

## 🔧 Common Operations

### Update Infrastructure

```bash
# Make changes to .tfvars file
nano terraform/environments/dev.tfvars

# Preview changes
./scripts/deploy.sh dev plan

# Apply changes
./scripts/deploy.sh dev apply
```

### View Outputs

```bash
# All outputs
terraform output

# Specific output
terraform output dynamodb_table_name

# Sensitive outputs
terraform output -raw iam_secret_access_key
```

### Destroy Infrastructure

⚠️ **WARNING**: This deletes all data!

```bash
./scripts/deploy.sh dev destroy
```

### Get Cost Estimate

```bash
./scripts/cost-estimate.sh dev
```

## 🔐 Security Best Practices

### 1. Protect Sensitive Files

The `.gitignore` is already configured to exclude:
- `*.tfvars` (contains configuration)
- `*.tfstate` (contains secrets)
- Access keys

**Never commit these files to Git!**

### 2. Rotate Credentials

```bash
# Force credential rotation
terraform taint aws_iam_access_key.backend_user
terraform apply

# Update .env file with new credentials
./scripts/get-credentials.sh dev env > ../.env
```

### 3. Use Different AWS Accounts

Best practice for production:
- Development: AWS Account A
- Staging: AWS Account B
- Production: AWS Account C

### 4. Enable MFA

Add MFA to IAM user for production:
```bash
aws iam enable-mfa-device \
  --user-name loyalty-backend-prod-backend-user \
  --serial-number arn:aws:iam::ACCOUNT:mfa/USER \
  --authentication-code-1 123456 \
  --authentication-code-2 789012
```

## 🐛 Troubleshooting

### Issue: "Error acquiring the state lock"

**Cause**: Another Terraform process is running or crashed

**Solution**:
```bash
# List locks
terraform force-unlock <LOCK_ID>
```

### Issue: "S3 bucket name already taken"

**Cause**: S3 bucket names are globally unique

**Solution**:
```bash
# Edit dev.tfvars
s3_bucket_name = "my-unique-bucket-name-12345"
```

### Issue: "Access Denied"

**Cause**: AWS credentials don't have sufficient permissions

**Solution**:
```bash
# Verify credentials
aws sts get-caller-identity

# Check IAM permissions in AWS Console
```

### Issue: "Error creating DynamoDB Table: ResourceInUseException"

**Cause**: Table already exists

**Solution**:
```bash
# Import existing table
terraform import aws_dynamodb_table.chat_messages ChatMessages

# Or delete existing table
aws dynamodb delete-table --table-name ChatMessages
```

## 📊 Monitoring

### View CloudWatch Alarms

```bash
# List alarms
aws cloudwatch describe-alarms \
  --alarm-names "loyalty-backend-dev-chat-messages-read-throttle"

# Check alarm history
aws cloudwatch describe-alarm-history \
  --alarm-name "loyalty-backend-dev-chat-messages-read-throttle"
```

### View DynamoDB Metrics

```bash
# Table metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/DynamoDB \
  --metric-name ConsumedReadCapacityUnits \
  --dimensions Name=TableName,Value=loyalty-backend-dev-chat-messages \
  --start-time 2025-10-26T00:00:00Z \
  --end-time 2025-10-26T23:59:59Z \
  --period 3600 \
  --statistics Average
```

### View S3 Metrics

```bash
# Bucket size
aws cloudwatch get-metric-statistics \
  --namespace AWS/S3 \
  --metric-name BucketSizeBytes \
  --dimensions Name=BucketName,Value=your-bucket-name Name=StorageType,Value=StandardStorage \
  --start-time 2025-10-26T00:00:00Z \
  --end-time 2025-10-26T23:59:59Z \
  --period 86400 \
  --statistics Average
```

## 💰 Cost Optimization

### Development
- Use `PAY_PER_REQUEST` billing
- Disable backups
- Disable logging
- **Estimated**: $10-15/month

### Production
- Use `PROVISIONED` with auto-scaling
- Enable backups
- Set up lifecycle policies
- **Estimated**: $30-50/month

### Cost Saving Tips

1. **Delete dev/staging infrastructure when not in use**:
   ```bash
   # Friday evening
   ./scripts/deploy.sh dev destroy

   # Monday morning
   ./scripts/deploy.sh dev apply
   ```

2. **Use S3 lifecycle policies**:
   - Archive to Glacier after 90 days: $0.004/GB
   - Delete after 365 days

3. **Monitor with AWS Budgets**:
   ```bash
   aws budgets create-budget \
     --account-id ACCOUNT_ID \
     --budget file://budget.json
   ```

4. **Use DynamoDB on-demand for unpredictable workloads**

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Terraform Deploy

on:
  push:
    branches: [main]
    paths: ['terraform/**']

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v2

      - name: Configure AWS
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Terraform Init
        run: terraform init
        working-directory: terraform

      - name: Terraform Plan
        run: terraform plan -var-file="environments/prod.tfvars"
        working-directory: terraform

      - name: Terraform Apply
        if: github.ref == 'refs/heads/main'
        run: terraform apply -auto-approve -var-file="environments/prod.tfvars"
        working-directory: terraform
```

## 📚 Additional Resources

- [Terraform Documentation](https://www.terraform.io/docs)
- [AWS DynamoDB Best Practices](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html)
- [AWS S3 Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/security-best-practices.html)
- [Full Terraform README](terraform/README.md)

## ✅ Checklist

Before deploying to production:

- [ ] Review all `.tfvars` files
- [ ] Update CORS origins with actual frontend URLs
- [ ] Enable point-in-time recovery for DynamoDB
- [ ] Enable S3 versioning
- [ ] Set up CloudWatch alarms with SNS alerts
- [ ] Configure AWS Budgets
- [ ] Test backup and restore procedures
- [ ] Document credential rotation process
- [ ] Set up monitoring dashboards
- [ ] Configure auto-scaling thresholds
- [ ] Review IAM policies (least privilege)
- [ ] Enable AWS CloudTrail for auditing

---

**Ready to deploy!** 🚀

Your infrastructure will be production-ready with proper security, monitoring, and scalability built-in.
