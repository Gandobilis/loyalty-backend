# AWS Setup Guide for Support Chat

This guide provides step-by-step instructions for setting up AWS infrastructure for the support chat feature.

## Prerequisites

- AWS Account
- AWS CLI installed and configured
- Appropriate IAM permissions to create resources

## Quick Setup Script

Save this as `setup-aws-chat.sh` and run it:

```bash
#!/bin/bash

# Configuration
REGION="us-east-1"
BUCKET_NAME="loyalty-chat-attachments"
DYNAMODB_TABLE="ChatMessages"
IAM_USER="loyalty-backend-chat"
IAM_POLICY="LoyaltyBackendChatPolicy"

echo "Setting up AWS infrastructure for Support Chat..."

# 1. Create DynamoDB Table
echo "Creating DynamoDB table: $DYNAMODB_TABLE"
aws dynamodb create-table \
  --table-name $DYNAMODB_TABLE \
  --attribute-definitions \
      AttributeName=MessageId,AttributeType=S \
      AttributeName=ChatId,AttributeType=N \
      AttributeName=Timestamp,AttributeType=N \
  --key-schema \
      AttributeName=MessageId,KeyType=HASH \
  --global-secondary-indexes \
      "[{
          \"IndexName\": \"ChatId-Timestamp-index\",
          \"KeySchema\": [
              {\"AttributeName\":\"ChatId\",\"KeyType\":\"HASH\"},
              {\"AttributeName\":\"Timestamp\",\"KeyType\":\"RANGE\"}
          ],
          \"Projection\":{\"ProjectionType\":\"ALL\"},
          \"ProvisionedThroughput\":{
              \"ReadCapacityUnits\":5,
              \"WriteCapacityUnits\":5
          }
      }]" \
  --provisioned-throughput \
      ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --region $REGION

echo "Waiting for table to be active..."
aws dynamodb wait table-exists --table-name $DYNAMODB_TABLE --region $REGION

# 2. Create S3 Bucket
echo "Creating S3 bucket: $BUCKET_NAME"
aws s3 mb s3://$BUCKET_NAME --region $REGION

# 3. Block public access
echo "Blocking public access to S3 bucket..."
aws s3api put-public-access-block \
  --bucket $BUCKET_NAME \
  --public-access-block-configuration \
      "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

# 4. Enable encryption
echo "Enabling S3 bucket encryption..."
aws s3api put-bucket-encryption \
  --bucket $BUCKET_NAME \
  --server-side-encryption-configuration \
      '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}'

# 5. Create CORS configuration
echo "Configuring CORS for S3 bucket..."
cat > /tmp/cors-config.json << 'EOF'
{
  "CORSRules": [
    {
      "AllowedOrigins": ["*"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["ETag"],
      "MaxAgeSeconds": 3600
    }
  ]
}
EOF

aws s3api put-bucket-cors \
  --bucket $BUCKET_NAME \
  --cors-configuration file:///tmp/cors-config.json

# 6. Create IAM Policy
echo "Creating IAM policy: $IAM_POLICY"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

cat > /tmp/iam-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:DeleteItem",
        "dynamodb:UpdateItem",
        "dynamodb:DescribeTable"
      ],
      "Resource": [
        "arn:aws:dynamodb:${REGION}:${ACCOUNT_ID}:table/${DYNAMODB_TABLE}",
        "arn:aws:dynamodb:${REGION}:${ACCOUNT_ID}:table/${DYNAMODB_TABLE}/index/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket",
        "s3:HeadBucket"
      ],
      "Resource": [
        "arn:aws:s3:::${BUCKET_NAME}",
        "arn:aws:s3:::${BUCKET_NAME}/*"
      ]
    }
  ]
}
EOF

aws iam create-policy \
  --policy-name $IAM_POLICY \
  --policy-document file:///tmp/iam-policy.json

# 7. Create IAM User
echo "Creating IAM user: $IAM_USER"
aws iam create-user --user-name $IAM_USER

# 8. Attach policy to user
echo "Attaching policy to user..."
aws iam attach-user-policy \
  --user-name $IAM_USER \
  --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/${IAM_POLICY}

# 9. Create access key
echo "Creating access key for user..."
aws iam create-access-key --user-name $IAM_USER > /tmp/access-key.json

# Extract credentials
ACCESS_KEY_ID=$(cat /tmp/access-key.json | jq -r '.AccessKey.AccessKeyId')
SECRET_ACCESS_KEY=$(cat /tmp/access-key.json | jq -r '.AccessKey.SecretAccessKey')

echo ""
echo "==============================================="
echo "AWS Infrastructure Setup Complete!"
echo "==============================================="
echo ""
echo "Add these to your .env file:"
echo ""
echo "AWS_REGION=$REGION"
echo "AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID"
echo "AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY"
echo "AWS_DYNAMODB_CHAT_TABLE=$DYNAMODB_TABLE"
echo "AWS_S3_BUCKET_NAME=$BUCKET_NAME"
echo ""
echo "IMPORTANT: Save these credentials securely!"
echo "==============================================="

# Cleanup temp files
rm /tmp/cors-config.json /tmp/iam-policy.json /tmp/access-key.json
```

## Manual Setup Instructions

### Step 1: Create DynamoDB Table

#### Using AWS Console:

1. Go to [DynamoDB Console](https://console.aws.amazon.com/dynamodb/)
2. Click "Create table"
3. Enter table details:
   - **Table name**: `ChatMessages`
   - **Partition key**: `MessageId` (String)
4. Under "Table settings", choose "Customize settings"
5. Set capacity mode to "Provisioned" (5 RCU, 5 WCU for development)
6. Click "Create index" to add Global Secondary Index:
   - **Index name**: `ChatId-Timestamp-index`
   - **Partition key**: `ChatId` (Number)
   - **Sort key**: `Timestamp` (Number)
   - **Projection type**: All
   - Capacity: 5 RCU, 5 WCU
7. Click "Create table"

#### Using AWS CLI:

```bash
aws dynamodb create-table \
  --table-name ChatMessages \
  --attribute-definitions \
      AttributeName=MessageId,AttributeType=S \
      AttributeName=ChatId,AttributeType=N \
      AttributeName=Timestamp,AttributeType=N \
  --key-schema \
      AttributeName=MessageId,KeyType=HASH \
  --global-secondary-indexes \
      "[{
          \"IndexName\": \"ChatId-Timestamp-index\",
          \"KeySchema\": [
              {\"AttributeName\":\"ChatId\",\"KeyType\":\"HASH\"},
              {\"AttributeName\":\"Timestamp\",\"KeyType\":\"RANGE\"}
          ],
          \"Projection\":{\"ProjectionType\":\"ALL\"},
          \"ProvisionedThroughput\":{
              \"ReadCapacityUnits\":5,
              \"WriteCapacityUnits\":5
          }
      }]" \
  --provisioned-throughput \
      ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --region us-east-1
```

### Step 2: Create S3 Bucket

#### Using AWS Console:

1. Go to [S3 Console](https://s3.console.aws.amazon.com/)
2. Click "Create bucket"
3. Enter bucket details:
   - **Bucket name**: `loyalty-chat-attachments` (must be globally unique)
   - **Region**: `us-east-1` (or your preferred region)
4. **Block Public Access**: Enable all options
5. **Bucket Versioning**: Disabled (optional: enable for file history)
6. **Encryption**: Enable with "SSE-S3"
7. Click "Create bucket"
8. Configure CORS:
   - Select your bucket
   - Go to "Permissions" tab
   - Scroll to "Cross-origin resource sharing (CORS)"
   - Click "Edit" and paste:
   ```json
   [
     {
       "AllowedHeaders": ["*"],
       "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
       "AllowedOrigins": ["http://localhost:3000", "https://your-frontend-domain.com"],
       "ExposeHeaders": ["ETag"],
       "MaxAgeSeconds": 3600
     }
   ]
   ```

#### Using AWS CLI:

```bash
# Create bucket
aws s3 mb s3://loyalty-chat-attachments --region us-east-1

# Block public access
aws s3api put-public-access-block \
  --bucket loyalty-chat-attachments \
  --public-access-block-configuration \
      "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

# Enable encryption
aws s3api put-bucket-encryption \
  --bucket loyalty-chat-attachments \
  --server-side-encryption-configuration \
      '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}'

# Configure CORS (create cors.json file first)
aws s3api put-bucket-cors \
  --bucket loyalty-chat-attachments \
  --cors-configuration file://cors.json
```

### Step 3: Create IAM Policy

#### Using AWS Console:

1. Go to [IAM Console](https://console.aws.amazon.com/iam/)
2. Click "Policies" in the left sidebar
3. Click "Create policy"
4. Choose "JSON" tab
5. Paste the policy document (replace `YOUR_ACCOUNT_ID` with your AWS account ID):

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:DeleteItem",
        "dynamodb:UpdateItem",
        "dynamodb:DescribeTable"
      ],
      "Resource": [
        "arn:aws:dynamodb:us-east-1:YOUR_ACCOUNT_ID:table/ChatMessages",
        "arn:aws:dynamodb:us-east-1:YOUR_ACCOUNT_ID:table/ChatMessages/index/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket",
        "s3:HeadBucket"
      ],
      "Resource": [
        "arn:aws:s3:::loyalty-chat-attachments",
        "arn:aws:s3:::loyalty-chat-attachments/*"
      ]
    }
  ]
}
```

6. Click "Next: Tags" (optional)
7. Click "Next: Review"
8. Enter policy name: `LoyaltyBackendChatPolicy`
9. Click "Create policy"

### Step 4: Create IAM User and Access Key

#### Using AWS Console:

1. In IAM Console, click "Users" in the left sidebar
2. Click "Add user"
3. Enter user name: `loyalty-backend-chat`
4. Select "Programmatic access"
5. Click "Next: Permissions"
6. Click "Attach existing policies directly"
7. Search for `LoyaltyBackendChatPolicy` and select it
8. Click "Next: Tags" (optional)
9. Click "Next: Review"
10. Click "Create user"
11. **IMPORTANT**: Copy the Access Key ID and Secret Access Key
12. Click "Download .csv" to save credentials

#### Using AWS CLI:

```bash
# Get your account ID
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Create policy
aws iam create-policy \
  --policy-name LoyaltyBackendChatPolicy \
  --policy-document file://policy.json

# Create user
aws iam create-user --user-name loyalty-backend-chat

# Attach policy to user
aws iam attach-user-policy \
  --user-name loyalty-backend-chat \
  --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/LoyaltyBackendChatPolicy

# Create access key
aws iam create-access-key --user-name loyalty-backend-chat
```

### Step 5: Configure Application

Add the following to your `.env` file:

```env
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key-id-here
AWS_SECRET_ACCESS_KEY=your-secret-access-key-here

# DynamoDB
AWS_DYNAMODB_CHAT_TABLE=ChatMessages

# S3
AWS_S3_BUCKET_NAME=loyalty-chat-attachments
AWS_S3_PRESIGNED_URL_DURATION=3600
```

## Testing the Setup

### Test DynamoDB Connection

```java
// Add this to your ChatService or create a test endpoint
@PostConstruct
public void testDynamoDbConnection() {
    try {
        dynamoDbService.createTableIfNotExists();
        log.info("DynamoDB connection successful!");
    } catch (Exception e) {
        log.error("DynamoDB connection failed", e);
    }
}
```

### Test S3 Connection

```java
// Add this to your S3FileService or create a test endpoint
@PostConstruct
public void testS3Connection() {
    try {
        s3FileService.ensureBucketExists();
        log.info("S3 connection successful!");
    } catch (Exception e) {
        log.error("S3 connection failed", e);
    }
}
```

### Test with curl

```bash
# Test creating a chat
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Test AWS Integration",
    "initialMessage": "Testing DynamoDB and S3 integration"
  }'
```

## Monitoring and Costs

### Enable CloudWatch Monitoring

1. Go to CloudWatch Console
2. Create dashboards for:
   - DynamoDB read/write capacity
   - S3 request metrics
   - Application logs

### Cost Monitoring

1. Go to AWS Cost Explorer
2. Filter by:
   - Service: DynamoDB, S3
   - Tag: Add tags to resources for better tracking

### Estimated Costs (Development)

- **DynamoDB**: $2.50/month (5 RCU/WCU)
- **S3 Storage**: $0.50/month (20GB)
- **S3 Requests**: $0.10/month
- **Data Transfer**: Variable

**Total**: ~$3-5/month for development

## Production Considerations

### DynamoDB

1. **Auto Scaling**: Enable auto scaling for RCU/WCU
   ```bash
   aws application-autoscaling register-scalable-target \
     --service-namespace dynamodb \
     --resource-id "table/ChatMessages" \
     --scalable-dimension dynamodb:table:ReadCapacityUnits \
     --min-capacity 5 \
     --max-capacity 100
   ```

2. **On-Demand Pricing**: Consider switching to on-demand for unpredictable workloads

3. **Point-in-Time Recovery**: Enable for data protection
   ```bash
   aws dynamodb update-continuous-backups \
     --table-name ChatMessages \
     --point-in-time-recovery-specification PointInTimeRecoveryEnabled=true
   ```

### S3

1. **Lifecycle Policies**: Archive old files to S3 Glacier
   ```json
   {
     "Rules": [
       {
         "Id": "ArchiveOldAttachments",
         "Status": "Enabled",
         "Transitions": [
           {
             "Days": 90,
             "StorageClass": "GLACIER"
           }
         ]
       }
     ]
   }
   ```

2. **CloudFront CDN**: Add CloudFront for faster file delivery
3. **S3 Intelligent-Tiering**: Automatically move files to cost-effective tiers

### Security

1. **Rotate Access Keys**: Rotate IAM credentials regularly
2. **Use IAM Roles**: For EC2/ECS deployments, use IAM roles instead of access keys
3. **Enable MFA**: Add MFA for sensitive operations
4. **Audit Logs**: Enable CloudTrail for API auditing

## Troubleshooting

### Issue: Access Denied to DynamoDB

**Solution:**
1. Verify IAM policy is attached to user
2. Check policy ARNs match your table
3. Ensure credentials are correct in `.env`

### Issue: S3 Bucket Not Found

**Solution:**
1. Verify bucket name is correct
2. Check region matches
3. Ensure credentials have s3:ListBucket permission

### Issue: CORS Errors

**Solution:**
1. Update CORS configuration with correct frontend URL
2. Verify preflight OPTIONS requests are allowed
3. Check browser console for specific CORS errors

### Issue: Presigned URL Expired

**Solution:**
1. Increase `AWS_S3_PRESIGNED_URL_DURATION` in application.properties
2. Generate new presigned URLs when serving files
3. Consider using CloudFront signed URLs for longer validity

## Cleanup (Delete All Resources)

⚠️ **WARNING**: This will delete all data!

```bash
#!/bin/bash

# Delete DynamoDB table
aws dynamodb delete-table --table-name ChatMessages

# Delete S3 bucket (must be empty first)
aws s3 rm s3://loyalty-chat-attachments --recursive
aws s3 rb s3://loyalty-chat-attachments

# Delete IAM user access keys
ACCESS_KEY_ID=$(aws iam list-access-keys --user-name loyalty-backend-chat --query 'AccessKeyMetadata[0].AccessKeyId' --output text)
aws iam delete-access-key --user-name loyalty-backend-chat --access-key-id $ACCESS_KEY_ID

# Detach policy from user
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
aws iam detach-user-policy \
  --user-name loyalty-backend-chat \
  --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/LoyaltyBackendChatPolicy

# Delete IAM user
aws iam delete-user --user-name loyalty-backend-chat

# Delete IAM policy
aws iam delete-policy \
  --policy-arn arn:aws:iam::${ACCOUNT_ID}:policy/LoyaltyBackendChatPolicy
```

## Next Steps

1. ✅ Set up AWS infrastructure
2. ✅ Configure application with AWS credentials
3. ✅ Test chat functionality
4. Configure monitoring and alerts
5. Set up auto-scaling for production
6. Implement backup and disaster recovery
7. Add CloudFront for CDN
8. Set up CI/CD pipeline for deployments

---

**Last Updated**: 2025-10-26
**Version**: 1.0
