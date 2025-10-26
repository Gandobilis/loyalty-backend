package com.multi.loyaltybackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * AWS SDK Configuration for DynamoDB, S3, SQS, and SNS.
 *
 * Environment Variables Required:
 * - AWS_REGION: AWS region (e.g., us-east-1)
 * - AWS_ACCESS_KEY_ID: AWS access key
 * - AWS_SECRET_ACCESS_KEY: AWS secret key
 * - AWS_DYNAMODB_ENDPOINT: (Optional) DynamoDB endpoint for local testing
 * - AWS_S3_ENDPOINT: (Optional) S3 endpoint for local testing
 */
@Configuration
@Slf4j
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access-key-id:}")
    private String awsAccessKeyId;

    @Value("${aws.secret-access-key:}")
    private String awsSecretAccessKey;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoDbEndpoint;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    /**
     * AWS Credentials Provider
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (awsAccessKeyId.isBlank() || awsSecretAccessKey.isBlank()) {
            log.warn("AWS credentials not configured. Using default credential provider chain.");
            return software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
        }

        log.info("Configuring AWS with static credentials for region: {}", awsRegion);
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)
        );
    }

    /**
     * DynamoDB Client for low-level operations
     */
    @Bean
    public DynamoDbClient dynamoDbClient(AwsCredentialsProvider credentialsProvider) {
        var builder = DynamoDbClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(credentialsProvider);

        if (!dynamoDbEndpoint.isBlank()) {
            log.info("Using custom DynamoDB endpoint: {}", dynamoDbEndpoint);
            builder.endpointOverride(java.net.URI.create(dynamoDbEndpoint));
        }

        return builder.build();
    }

    /**
     * DynamoDB Enhanced Client for object mapping
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }

    /**
     * S3 Client for file operations
     */
    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        var builder = S3Client.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(credentialsProvider);

        if (!s3Endpoint.isBlank()) {
            log.info("Using custom S3 endpoint: {}", s3Endpoint);
            builder.endpointOverride(java.net.URI.create(s3Endpoint));
        }

        return builder.build();
    }

    /**
     * S3 Presigner for generating presigned URLs
     */
    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        var builder = S3Presigner.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(credentialsProvider);

        if (!s3Endpoint.isBlank()) {
            builder.endpointOverride(java.net.URI.create(s3Endpoint));
        }

        return builder.build();
    }

    /**
     * SQS Client for message queue
     */
    @Bean
    public SqsClient sqsClient(AwsCredentialsProvider credentialsProvider) {
        return SqsClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    /**
     * SNS Client for notifications
     */
    @Bean
    public SnsClient snsClient(AwsCredentialsProvider credentialsProvider) {
        return SnsClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(credentialsProvider)
            .build();
    }
}
