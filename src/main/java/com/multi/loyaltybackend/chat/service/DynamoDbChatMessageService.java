package com.multi.loyaltybackend.chat.service;

import com.multi.loyaltybackend.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for storing and retrieving chat messages from DynamoDB.
 * This provides high-performance message storage alongside PostgreSQL.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbChatMessageService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.chat-messages-table:ChatMessages}")
    private String tableName;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Save a message to DynamoDB
     */
    public String saveMessage(ChatMessage message) {
        try {
            String messageId = UUID.randomUUID().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("MessageId", AttributeValue.builder().s(messageId).build());
            item.put("ChatId", AttributeValue.builder().n(String.valueOf(message.getChat().getId())).build());
            item.put("SenderId", AttributeValue.builder().n(String.valueOf(message.getSender().getId())).build());
            item.put("SenderName", AttributeValue.builder().s(message.getSender().getFullName()).build());
            item.put("SenderEmail", AttributeValue.builder().s(message.getSender().getEmail()).build());
            item.put("Content", AttributeValue.builder().s(message.getContent()).build());
            item.put("MessageType", AttributeValue.builder().s(message.getMessageType().name()).build());
            item.put("IsRead", AttributeValue.builder().bool(message.getIsRead()).build());
            item.put("IsAdminMessage", AttributeValue.builder().bool(message.getIsAdminMessage()).build());
            item.put("CreatedAt", AttributeValue.builder().s(message.getCreatedAt().format(FORMATTER)).build());
            item.put("Timestamp", AttributeValue.builder().n(String.valueOf(System.currentTimeMillis())).build());

            // Add optional fields
            if (message.getAttachmentS3Key() != null) {
                item.put("AttachmentS3Key", AttributeValue.builder().s(message.getAttachmentS3Key()).build());
                item.put("AttachmentFilename", AttributeValue.builder().s(message.getAttachmentFilename()).build());
                item.put("AttachmentSize", AttributeValue.builder().n(String.valueOf(message.getAttachmentSize())).build());
                item.put("AttachmentMimeType", AttributeValue.builder().s(message.getAttachmentMimeType()).build());
            }

            if (message.getReadAt() != null) {
                item.put("ReadAt", AttributeValue.builder().s(message.getReadAt().format(FORMATTER)).build());
            }

            PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

            dynamoDbClient.putItem(request);
            log.info("Saved message to DynamoDB: {}", messageId);

            return messageId;
        } catch (Exception e) {
            log.error("Failed to save message to DynamoDB", e);
            throw new RuntimeException("Failed to save message to DynamoDB", e);
        }
    }

    /**
     * Get messages for a chat from DynamoDB
     */
    public List<Map<String, AttributeValue>> getMessagesForChat(Long chatId, int limit) {
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":chatId", AttributeValue.builder().n(String.valueOf(chatId)).build());

            QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName("ChatId-Timestamp-index") // GSI needed
                .keyConditionExpression("ChatId = :chatId")
                .expressionAttributeValues(expressionAttributeValues)
                .limit(limit)
                .scanIndexForward(false) // Most recent first
                .build();

            QueryResponse response = dynamoDbClient.query(request);
            return response.items();
        } catch (Exception e) {
            log.error("Failed to get messages from DynamoDB for chat: {}", chatId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Delete all messages for a chat
     */
    public void deleteMessagesForChat(Long chatId) {
        try {
            List<Map<String, AttributeValue>> messages = getMessagesForChat(chatId, 1000);

            for (Map<String, AttributeValue> message : messages) {
                Map<String, AttributeValue> key = new HashMap<>();
                key.put("MessageId", message.get("MessageId"));

                DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

                dynamoDbClient.deleteItem(request);
            }

            log.info("Deleted {} messages from DynamoDB for chat: {}", messages.size(), chatId);
        } catch (Exception e) {
            log.error("Failed to delete messages from DynamoDB for chat: {}", chatId, e);
        }
    }

    /**
     * Create DynamoDB table if it doesn't exist
     */
    public void createTableIfNotExists() {
        try {
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();

            dynamoDbClient.describeTable(describeRequest);
            log.info("DynamoDB table already exists: {}", tableName);
        } catch (ResourceNotFoundException e) {
            log.info("Creating DynamoDB table: {}", tableName);
            createTable();
        }
    }

    private void createTable() {
        CreateTableRequest request = CreateTableRequest.builder()
            .tableName(tableName)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName("MessageId")
                    .keyType(KeyType.HASH)
                    .build()
            )
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("MessageId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("ChatId")
                    .attributeType(ScalarAttributeType.N)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("Timestamp")
                    .attributeType(ScalarAttributeType.N)
                    .build()
            )
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName("ChatId-Timestamp-index")
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName("ChatId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            .attributeName("Timestamp")
                            .keyType(KeyType.RANGE)
                            .build()
                    )
                    .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                    .build()
            )
            .provisionedThroughput(ProvisionedThroughput.builder()
                .readCapacityUnits(5L)
                .writeCapacityUnits(5L)
                .build())
            .build();

        dynamoDbClient.createTable(request);
        log.info("DynamoDB table created: {}", tableName);
    }
}
