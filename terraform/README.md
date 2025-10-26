# Terraform Infrastructure for Support Chat

This directory contains Terraform configurations to provision AWS infrastructure for the Loyalty Backend support chat feature.

## 📁 Directory Structure

```
terraform/
├── main.tf                      # Main Terraform configuration
├── variables.tf                 # Variable definitions
├── outputs.tf                   # Output definitions
├── terraform.tfvars.example     # Example variables file
├── .gitignore                   # Git ignore rules
├── environments/                # Environment-specific configurations
│   ├── dev.tfvars              # Development environment
│   ├── staging.tfvars          # Staging environment
│   └── prod.tfvars             # Production environment
└── README.md                    # This file
```

## 🚀 Quick Start

### Prerequisites

1. **Install Terraform** (version >= 1.0):
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

2. **Configure AWS CLI**:
   ```bash
   aws configure
   ```

3. **Verify Installation**:
   ```bash
   terraform version
   aws sts get-caller-identity
   ```

### Initial Setup

1. **Navigate to terraform directory**:
   ```bash
   cd terraform
   ```

2. **Initialize Terraform**:
   ```bash
   terraform init
   ```

3. **Create your variables file**:
   ```bash
   # Copy the example file
   cp terraform.tfvars.example terraform.tfvars

   # Edit with your values
   nano terraform.tfvars
   ```

4. **Validate configuration**:
   ```bash
   terraform validate
   ```

5. **Preview changes**:
   ```bash
   terraform plan
   ```

6. **Apply infrastructure**:
   ```bash
   terraform apply
   ```

## 🌍 Environment-Specific Deployments

### Development Environment

```bash
terraform workspace new dev
terraform workspace select dev
terraform plan -var-file="environments/dev.tfvars"
terraform apply -var-file="environments/dev.tfvars"
```

### Staging Environment

```bash
terraform workspace new staging
terraform workspace select staging
terraform plan -var-file="environments/staging.tfvars"
terraform apply -var-file="environments/staging.tfvars"
```

### Production Environment

```bash
terraform workspace new prod
terraform workspace select prod
terraform plan -var-file="environments/prod.tfvars"
terraform apply -var-file="environments/prod.tfvars"
```

## 📦 Resources Created

This Terraform configuration creates the following AWS resources:

### Core Resources (Always Created)

1. **DynamoDB Table**: `loyalty-backend-{env}-chat-messages`
   - Hash Key: MessageId (String)
   - Global Secondary Index: ChatId-Timestamp-index
   - Encryption: Enabled
   - Point-in-Time Recovery: Configurable

2. **S3 Bucket**: `loyalty-backend-{env}-chat-attachments-{account-id}`
   - Encryption: AES256 or KMS
   - Public Access: Blocked
   - CORS: Configured for frontend
   - Lifecycle Policy: Configurable

3. **IAM User**: `loyalty-backend-{env}-backend-user`
   - Access Key: Auto-generated
   - Policies: DynamoDB and S3 access

4. **IAM Policies**:
   - DynamoDB access policy
   - S3 access policy

### Optional Resources (Configurable)

5. **SQS Queue**: `loyalty-backend-{env}-chat-notifications` (if `enable_sqs = true`)
   - Dead Letter Queue included
   - Encryption: Enabled

6. **SNS Topic**: `loyalty-backend-{env}-chat-alerts` (if `enable_sns = true`)
   - Email subscriptions: Configurable
   - Encryption: Optional KMS

7. **CloudWatch Alarms** (if `enable_cloudwatch_alarms = true`):
   - DynamoDB read throttle alarm
   - DynamoDB write throttle alarm
   - S3 bucket size alarm

8. **Auto-Scaling** (if `enable_autoscaling = true` and `billing_mode = PROVISIONED`):
   - DynamoDB table read capacity
   - DynamoDB table write capacity

## 🔧 Configuration Options

### Key Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `aws_region` | AWS region | `us-east-1` | No |
| `project_name` | Project name | `loyalty-backend` | No |
| `environment` | Environment (dev/staging/prod) | `dev` | No |
| `dynamodb_billing_mode` | PROVISIONED or PAY_PER_REQUEST | `PAY_PER_REQUEST` | No |
| `enable_sqs` | Enable SQS queue | `false` | No |
| `enable_sns` | Enable SNS topic | `false` | No |
| `cors_allowed_origins` | CORS allowed origins | `["http://localhost:3000"]` | No |

See `variables.tf` for complete list of variables.

### Cost Optimization

**Development**:
- Use `PAY_PER_REQUEST` billing mode
- Disable point-in-time recovery
- Disable S3 lifecycle policies
- Disable logging

**Production**:
- Use `PROVISIONED` billing mode with auto-scaling
- Enable point-in-time recovery
- Enable S3 lifecycle policies
- Enable CloudWatch alarms

## 📊 Outputs

After applying the configuration, Terraform will output:

```bash
# View all outputs
terraform output

# View specific output
terraform output dynamodb_table_name
terraform output s3_bucket_name

# View sensitive outputs
terraform output -raw iam_secret_access_key
```

### Important Outputs

- `application_env_vars`: Environment variables for your .env file
- `dynamodb_table_name`: DynamoDB table name
- `s3_bucket_name`: S3 bucket name
- `iam_access_key_id`: AWS Access Key ID (sensitive)
- `iam_secret_access_key`: AWS Secret Access Key (sensitive)

### Save Credentials to .env

```bash
# Extract credentials and save to .env
terraform output -raw application_env_vars >> ../.env
```

## 🔐 Security Best Practices

1. **Never commit sensitive files**:
   - ✅ `.gitignore` is configured to exclude `*.tfvars` and state files
   - ✅ State files contain sensitive data

2. **Use remote state backend** (for teams):
   ```hcl
   # Uncomment in main.tf
   backend "s3" {
     bucket         = "your-terraform-state-bucket"
     key            = "chat/terraform.tfstate"
     region         = "us-east-1"
     encrypt        = true
     dynamodb_table = "terraform-state-lock"
   }
   ```

3. **Enable state locking**:
   - Create DynamoDB table for state locking
   - Prevents concurrent modifications

4. **Rotate IAM credentials regularly**:
   ```bash
   # To rotate credentials, delete and recreate access key
   terraform taint aws_iam_access_key.backend_user
   terraform apply
   ```

5. **Use KMS encryption** (production):
   ```hcl
   kms_key_arn     = "arn:aws:kms:us-east-1:123456789012:key/..."
   s3_kms_key_arn  = "arn:aws:kms:us-east-1:123456789012:key/..."
   sns_kms_key_arn = "arn:aws:kms:us-east-1:123456789012:key/..."
   ```

## 🔄 Common Operations

### Update Infrastructure

```bash
# Make changes to .tf files or .tfvars
terraform plan
terraform apply
```

### Destroy Infrastructure

⚠️ **WARNING**: This will delete all resources and data!

```bash
# Preview what will be destroyed
terraform plan -destroy

# Destroy all resources
terraform destroy

# Destroy specific environment
terraform destroy -var-file="environments/dev.tfvars"
```

### Import Existing Resources

```bash
# Import existing DynamoDB table
terraform import aws_dynamodb_table.chat_messages ChatMessages

# Import existing S3 bucket
terraform import aws_s3_bucket.chat_attachments loyalty-chat-attachments
```

### View Current State

```bash
# List resources in state
terraform state list

# Show specific resource
terraform state show aws_dynamodb_table.chat_messages

# Show all state
terraform show
```

### Format Code

```bash
# Format all .tf files
terraform fmt -recursive
```

## 📈 Monitoring and Maintenance

### CloudWatch Dashboards

Create custom dashboards to monitor:
- DynamoDB read/write capacity utilization
- S3 bucket size and requests
- SQS queue depth
- Lambda errors (if using DynamoDB Streams)

### Cost Monitoring

1. **Enable AWS Cost Explorer**
2. **Tag all resources** (automatically done by Terraform)
3. **Create cost alerts**:
   ```bash
   # Add to SNS subscriptions in tfvars
   sns_email_subscriptions = ["billing@example.com"]
   ```

### Backup Strategy

1. **DynamoDB**:
   - Point-in-Time Recovery enabled (35 days)
   - On-demand backups: Use AWS Backup

2. **S3**:
   - Versioning enabled (optional)
   - Cross-region replication (for disaster recovery)

## 🐛 Troubleshooting

### Issue: "Error acquiring the state lock"

**Solution**:
```bash
# Force unlock (use with caution!)
terraform force-unlock <LOCK_ID>
```

### Issue: "Resource already exists"

**Solution**:
```bash
# Import the existing resource
terraform import <resource_type>.<resource_name> <resource_id>
```

### Issue: "Access Denied"

**Solution**:
```bash
# Check AWS credentials
aws sts get-caller-identity

# Ensure IAM user has sufficient permissions
```

### Issue: "S3 bucket name already taken"

**Solution**:
```bash
# S3 bucket names must be globally unique
# Set a custom name in tfvars:
s3_bucket_name = "my-unique-bucket-name-12345"
```

## 🔄 Migration from Manual Setup

If you've already created resources manually using the AWS setup guide:

1. **Import existing resources**:
   ```bash
   # DynamoDB
   terraform import aws_dynamodb_table.chat_messages ChatMessages

   # S3
   terraform import aws_s3_bucket.chat_attachments loyalty-chat-attachments

   # IAM User
   terraform import aws_iam_user.backend_user loyalty-backend-chat
   ```

2. **Verify state**:
   ```bash
   terraform plan
   # Should show no changes if imports match configuration
   ```

## 📚 Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)
- [AWS DynamoDB Pricing](https://aws.amazon.com/dynamodb/pricing/)
- [AWS S3 Pricing](https://aws.amazon.com/s3/pricing/)

## 🆘 Support

For issues or questions:
1. Check Terraform logs: `TF_LOG=DEBUG terraform apply`
2. Review AWS CloudTrail for API errors
3. Verify AWS service quotas
4. Check [Terraform AWS Provider Issues](https://github.com/hashicorp/terraform-provider-aws/issues)

## 📝 Change Log

- **v1.0** (2025-10-26): Initial Terraform configuration
  - DynamoDB table with GSI
  - S3 bucket with lifecycle policies
  - IAM user and policies
  - Optional SQS and SNS
  - CloudWatch alarms
  - Auto-scaling support

---

**Maintained by**: DevOps Team
**Last Updated**: 2025-10-26
