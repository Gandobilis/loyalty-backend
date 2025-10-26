# Support Chat Implementation Guide

## Overview

This document describes the complete implementation of the support chat functionality for the Loyalty Backend application using AWS services.

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATION                       │
│  (Web/Mobile Frontend with WebSocket & REST API Support)        │
└────────────────┬───────────────────────────┬────────────────────┘
                 │                           │
          REST API Calls              WebSocket Connection
                 │                           │
                 ▼                           ▼
┌──────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                        │
│                                                                   │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ ChatController  │  │ WebSocketChat    │  │  ChatService    │ │
│  │   (REST API)    │  │   Controller     │  │ (Business Logic)│ │
│  └─────────────────┘  └──────────────────┘  └─────────────────┘ │
│           │                    │                      │           │
│           └────────────────────┼──────────────────────┘           │
│                                │                                  │
│  ┌─────────────────────────────┼────────────────────────────────┐│
│  │              Service Layer Integration                       ││
│  │  ┌──────────────┐  ┌─────────────┐  ┌──────────────────┐   ││
│  │  │ DynamoDB     │  │ S3 File     │  │ PostgreSQL       │   ││
│  │  │ Service      │  │ Service     │  │ Repositories     │   ││
│  │  └──────────────┘  └─────────────┘  └──────────────────┘   ││
│  └────────┬───────────────┬─────────────────┬──────────────────┘│
└───────────┼───────────────┼─────────────────┼───────────────────┘
            │               │                 │
            ▼               ▼                 ▼
   ┌────────────────┐  ┌──────────┐  ┌────────────────┐
   │   DynamoDB     │  │    S3    │  │  PostgreSQL    │
   │ (Chat Messages)│  │  (Files) │  │  (Metadata)    │
   └────────────────┘  └──────────┘  └────────────────┘
```

### Data Storage Strategy

1. **PostgreSQL** (Relational Data)
   - Chat metadata (subject, status, participants)
   - User information
   - Chat-to-user relationships
   - Message metadata (small footprint)
   - Aggregate statistics (message counts, unread counts)

2. **DynamoDB** (High-Performance Message Storage)
   - Full message content
   - Message history
   - Fast retrieval for recent messages
   - Automatic scaling
   - TTL support for message expiration

3. **S3** (File Storage)
   - Chat attachments (images, documents)
   - Presigned URLs for secure access
   - Lifecycle policies for cost optimization

## Implementation Details

### 1. Database Schema (PostgreSQL)

#### Chats Table
```sql
CREATE TABLE chats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    assigned_to_id BIGINT REFERENCES users(id),
    subject VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    dynamodb_table_reference VARCHAR(255),
    message_count INTEGER NOT NULL DEFAULT 0,
    unread_by_user INTEGER NOT NULL DEFAULT 0,
    unread_by_admin INTEGER NOT NULL DEFAULT 0,
    last_message_preview VARCHAR(500),
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_assigned_status (assigned_to_id, status),
    INDEX idx_last_message (last_message_at DESC)
);
```

#### Chat Messages Table
```sql
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    attachment_s3_key VARCHAR(500),
    attachment_filename VARCHAR(255),
    attachment_size BIGINT,
    attachment_mime_type VARCHAR(100),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    is_admin_message BOOLEAN NOT NULL DEFAULT FALSE,
    dynamodb_message_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    INDEX idx_chat_created (chat_id, created_at),
    INDEX idx_sender_created (sender_id, created_at)
);
```

### 2. DynamoDB Schema

#### ChatMessages Table

**Primary Key:**
- Partition Key: `MessageId` (String, UUID)

**Attributes:**
- `MessageId`: String (UUID)
- `ChatId`: Number
- `SenderId`: Number
- `SenderName`: String
- `SenderEmail`: String
- `Content`: String (max 5000 chars)
- `MessageType`: String (TEXT, FILE, SYSTEM, IMAGE)
- `IsRead`: Boolean
- `IsAdminMessage`: Boolean
- `CreatedAt`: String (ISO-8601 format)
- `Timestamp`: Number (Unix timestamp in milliseconds)
- `AttachmentS3Key`: String (optional)
- `AttachmentFilename`: String (optional)
- `AttachmentSize`: Number (optional)
- `AttachmentMimeType`: String (optional)
- `ReadAt`: String (optional)

**Global Secondary Index (GSI):**
- Name: `ChatId-Timestamp-index`
- Partition Key: `ChatId` (Number)
- Sort Key: `Timestamp` (Number)
- Projection: ALL

**Provisioning:**
- Read Capacity: 5 units (adjustable)
- Write Capacity: 5 units (adjustable)

### 3. S3 Bucket Structure

```
loyalty-chat-attachments/
├── chat-attachments/
│   ├── {chatId}/
│   │   ├── {uuid}.jpg
│   │   ├── {uuid}.pdf
│   │   └── {uuid}.docx
```

**Bucket Configuration:**
- Versioning: Disabled
- Encryption: AES-256 (SSE-S3)
- CORS: Configured for frontend domain
- Lifecycle Policy: Delete after 90 days (optional)
- Public Access: Blocked (use presigned URLs)

## API Endpoints

### REST API Endpoints

#### User Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/chat` | Create a new support chat | User |
| `GET` | `/api/chat` | Get all user's chats | User |
| `GET` | `/api/chat/{chatId}` | Get specific chat details | User |
| `GET` | `/api/chat/{chatId}/messages` | Get messages (paginated) | User |
| `POST` | `/api/chat/{chatId}/messages` | Send message with attachment | User |
| `POST` | `/api/chat/{chatId}/messages/text` | Send text-only message | User |
| `PUT` | `/api/chat/{chatId}/read` | Mark messages as read | User |
| `PUT` | `/api/chat/{chatId}/close` | Close a chat | User |
| `DELETE` | `/api/chat/{chatId}` | Delete a chat | User |

#### Admin Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/chat/admin/all` | Get all chats with filters | Admin |
| `PUT` | `/api/chat/admin/{chatId}/assign` | Assign chat to agent | Admin |

### WebSocket Endpoints

**Connection:**
- Endpoint: `/ws` with SockJS fallback
- Protocol: STOMP over WebSocket

**Subscriptions (Client subscribes to):**
- `/topic/chat/{chatId}` - Receive new messages
- `/topic/chat/{chatId}/typing` - Receive typing indicators
- `/topic/chat/{chatId}/read` - Receive read receipts
- `/topic/chat/{chatId}/presence` - Receive join/leave notifications

**Publishing (Client sends to):**
- `/app/chat/{chatId}/send` - Send a message
- `/app/chat/{chatId}/typing` - Notify typing
- `/app/chat/{chatId}/read` - Mark as read
- `/app/chat/{chatId}/join` - Join chat room
- `/app/chat/{chatId}/leave` - Leave chat room

## Request/Response Examples

### Create a New Chat

**Request:**
```http
POST /api/chat
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "subject": "Need help with loyalty points",
  "initialMessage": "I noticed my points haven't been credited after my recent purchase."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 456,
    "userFullName": "John Doe",
    "userEmail": "john@example.com",
    "assignedToId": null,
    "assignedToFullName": null,
    "subject": "Need help with loyalty points",
    "status": "OPEN",
    "messageCount": 1,
    "unreadByUser": 0,
    "unreadByAdmin": 1,
    "lastMessagePreview": "I noticed my points haven't been credited...",
    "lastMessageAt": "2025-10-26T10:30:00",
    "createdAt": "2025-10-26T10:30:00",
    "updatedAt": "2025-10-26T10:30:00",
    "closedAt": null
  },
  "timestamp": "2025-10-26T10:30:00",
  "correlationId": "abc-123-def",
  "path": "/api/chat"
}
```

### Send a Message with Attachment

**Request:**
```http
POST /api/chat/123/messages
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data

message: {
  "content": "Here's the screenshot of the issue",
  "messageType": "FILE"
}
attachment: <file binary data>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 789,
    "chatId": 123,
    "senderId": 456,
    "senderFullName": "John Doe",
    "senderEmail": "john@example.com",
    "content": "Here's the screenshot of the issue",
    "messageType": "FILE",
    "attachmentS3Key": "chat-attachments/123/abc-def-123.png",
    "attachmentFilename": "screenshot.png",
    "attachmentSize": 125000,
    "attachmentMimeType": "image/png",
    "attachmentUrl": "https://s3.amazonaws.com/...",
    "isRead": false,
    "readAt": null,
    "isAdminMessage": false,
    "createdAt": "2025-10-26T10:35:00"
  },
  "timestamp": "2025-10-26T10:35:00"
}
```

### WebSocket Message Example

**Client sends:**
```javascript
stompClient.send('/app/chat/123/send', {}, JSON.stringify({
  content: 'Thank you for your help!',
  messageType: 'TEXT'
}));
```

**Server broadcasts to all subscribers on `/topic/chat/123`:**
```json
{
  "type": "MESSAGE",
  "chatId": 123,
  "messageId": 790,
  "senderId": 456,
  "senderFullName": "John Doe",
  "content": "Thank you for your help!",
  "messageType": "TEXT",
  "attachmentUrl": null,
  "timestamp": "2025-10-26T10:40:00"
}
```

## Environment Variables

Add these to your `.env` file:

```env
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key

# DynamoDB Configuration
AWS_DYNAMODB_ENDPOINT=          # Leave empty for AWS, set for local testing
AWS_DYNAMODB_CHAT_TABLE=ChatMessages

# S3 Configuration
AWS_S3_ENDPOINT=                # Leave empty for AWS, set for local testing
AWS_S3_BUCKET_NAME=loyalty-chat-attachments
AWS_S3_PRESIGNED_URL_DURATION=3600  # 1 hour in seconds

# SQS Configuration (optional)
AWS_SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/...

# SNS Configuration (optional)
AWS_SNS_TOPIC_ARN=arn:aws:sns:us-east-1:...
```

## AWS Setup Instructions

### 1. Create DynamoDB Table

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

### 2. Create S3 Bucket

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

# Configure CORS
aws s3api put-bucket-cors \
  --bucket loyalty-chat-attachments \
  --cors-configuration file://cors-config.json
```

**cors-config.json:**
```json
{
  "CORSRules": [
    {
      "AllowedOrigins": ["http://localhost:3000", "https://your-frontend-domain.com"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["ETag"],
      "MaxAgeSeconds": 3600
    }
  ]
}
```

### 3. Create IAM Policy

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
        "dynamodb:UpdateItem"
      ],
      "Resource": [
        "arn:aws:dynamodb:us-east-1:*:table/ChatMessages",
        "arn:aws:dynamodb:us-east-1:*:table/ChatMessages/index/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::loyalty-chat-attachments",
        "arn:aws:s3:::loyalty-chat-attachments/*"
      ]
    }
  ]
}
```

### 4. Create IAM User

```bash
# Create user
aws iam create-user --user-name loyalty-backend-chat

# Attach policy
aws iam attach-user-policy \
  --user-name loyalty-backend-chat \
  --policy-arn arn:aws:iam::YOUR_ACCOUNT_ID:policy/LoyaltyBackendChatPolicy

# Create access key
aws iam create-access-key --user-name loyalty-backend-chat
```

## Frontend Integration Examples

### REST API Integration (React/JavaScript)

```javascript
// Create a new chat
async function createChat(subject, initialMessage) {
  const response = await fetch('http://localhost:8080/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ subject, initialMessage })
  });

  return await response.json();
}

// Send message with attachment
async function sendMessageWithAttachment(chatId, content, file) {
  const formData = new FormData();
  formData.append('message', new Blob([JSON.stringify({
    content: content,
    messageType: 'FILE'
  })], { type: 'application/json' }));
  formData.append('attachment', file);

  const response = await fetch(`http://localhost:8080/api/chat/${chatId}/messages`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });

  return await response.json();
}
```

### WebSocket Integration (React/SockJS/STOMP)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class ChatWebSocketService {
  constructor() {
    this.stompClient = null;
  }

  connect(token, onConnected, onError) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    const headers = {
      'Authorization': `Bearer ${token}`
    };

    this.stompClient.connect(headers, onConnected, onError);
  }

  subscribeToChat(chatId, onMessageReceived) {
    return this.stompClient.subscribe(
      `/topic/chat/${chatId}`,
      (message) => {
        const parsedMessage = JSON.parse(message.body);
        onMessageReceived(parsedMessage);
      }
    );
  }

  sendMessage(chatId, content) {
    this.stompClient.send(
      `/app/chat/${chatId}/send`,
      {},
      JSON.stringify({
        content: content,
        messageType: 'TEXT'
      })
    );
  }

  sendTypingIndicator(chatId) {
    this.stompClient.send(`/app/chat/${chatId}/typing`, {}, {});
  }

  markAsRead(chatId) {
    this.stompClient.send(`/app/chat/${chatId}/read`, {}, {});
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  }
}

export default new ChatWebSocketService();
```

### React Component Example

```jsx
import React, { useEffect, useState } from 'react';
import chatWebSocket from './ChatWebSocketService';

function ChatComponent({ chatId, token }) {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');

  useEffect(() => {
    // Connect to WebSocket
    chatWebSocket.connect(
      token,
      () => {
        console.log('Connected to WebSocket');

        // Subscribe to chat
        chatWebSocket.subscribeToChat(chatId, (message) => {
          if (message.type === 'MESSAGE') {
            setMessages(prev => [...prev, message]);
          }
        });
      },
      (error) => {
        console.error('WebSocket error:', error);
      }
    );

    return () => {
      chatWebSocket.disconnect();
    };
  }, [chatId, token]);

  const sendMessage = () => {
    if (newMessage.trim()) {
      chatWebSocket.sendMessage(chatId, newMessage);
      setNewMessage('');
    }
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index} className="message">
            <strong>{msg.senderFullName}:</strong> {msg.content}
          </div>
        ))}
      </div>
      <div className="input-area">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
}
```

## Testing

### Local Testing with LocalStack

For local development, you can use LocalStack to simulate AWS services:

```bash
# Install LocalStack
pip install localstack

# Start LocalStack
localstack start

# Set environment variables for local testing
export AWS_DYNAMODB_ENDPOINT=http://localhost:4566
export AWS_S3_ENDPOINT=http://localhost:4566
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
```

### Testing REST API

```bash
# Create a chat
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Test Chat",
    "initialMessage": "Hello, I need help!"
  }'

# Get all chats
curl -X GET http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Send a message
curl -X POST http://localhost:8080/api/chat/1/messages/text \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is a test message"
  }'
```

## Performance Considerations

1. **Message Pagination**: Messages are paginated (default 50 per page) to reduce data transfer
2. **Presigned URLs**: S3 presigned URLs expire after 1 hour by default
3. **WebSocket Connection Limits**: Monitor concurrent WebSocket connections
4. **DynamoDB Capacity**: Start with 5 RCU/WCU, scale based on usage
5. **S3 Lifecycle**: Consider archiving old attachments after 90 days

## Monitoring & Logging

1. **Application Logs**: All chat operations are logged with correlation IDs
2. **AWS CloudWatch**: Monitor DynamoDB and S3 metrics
3. **WebSocket Monitoring**: Track connection counts and message throughput
4. **Error Tracking**: All exceptions are logged with context

## Security Best Practices

1. **Authentication**: All endpoints require JWT authentication
2. **Authorization**: Users can only access their own chats (admins can access all)
3. **File Validation**: File uploads are validated for type and size
4. **Presigned URLs**: S3 access is controlled via time-limited presigned URLs
5. **CORS**: Properly configured CORS for frontend domain
6. **Encryption**: All data encrypted at rest (S3, DynamoDB)

## Cost Estimation (Monthly)

### AWS Services (Small Scale: 1000 users, 10k messages/month)

- **DynamoDB**: ~$5-10 (5 RCU/WCU)
- **S3 Storage**: ~$2-5 (20GB storage)
- **S3 Requests**: ~$1-2
- **Data Transfer**: ~$5-10

**Total Estimated Cost**: ~$15-30/month

For production with higher traffic, consider:
- DynamoDB Auto Scaling
- S3 Intelligent-Tiering
- CloudFront for S3 content delivery

## Troubleshooting

### Common Issues

1. **WebSocket Connection Fails**
   - Check CORS configuration
   - Verify JWT token is valid
   - Ensure `/ws` endpoint is accessible

2. **File Upload Fails**
   - Check S3 bucket permissions
   - Verify file size limits
   - Check allowed MIME types

3. **Messages Not Appearing in DynamoDB**
   - Verify AWS credentials
   - Check DynamoDB table name
   - Verify GSI is created

4. **Access Denied Errors**
   - Verify IAM policy permissions
   - Check user authentication
   - Verify chat ownership

## Future Enhancements

1. **Message Search**: Full-text search using AWS OpenSearch
2. **File Compression**: Compress images before upload
3. **Message Reactions**: Add emoji reactions to messages
4. **Chat Templates**: Pre-defined response templates for admins
5. **Analytics Dashboard**: Chat metrics and performance insights
6. **Multi-language Support**: Automatic translation
7. **Voice/Video Chat**: Integration with AWS Chime SDK
8. **Chatbot Integration**: AI-powered auto-responses using AWS Lex

## Support

For issues or questions:
- Check application logs with correlation IDs
- Review AWS CloudWatch logs
- Verify environment variables are set correctly
- Test with local stack before deploying to AWS

---

**Implementation Date**: 2025-10-26
**Version**: 1.0
**Author**: Claude Code Assistant
