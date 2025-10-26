package com.multi.loyaltybackend.chat.service;

import com.multi.loyaltybackend.chat.exception.InvalidFileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file uploads to S3 for chat attachments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name:loyalty-chat-attachments}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-duration:3600}") // Default 1 hour
    private long presignedUrlDuration;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain", "text/csv"
    );

    /**
     * Upload a file to S3
     */
    public String uploadFile(MultipartFile file, Long chatId) {
        validateFile(file);

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String s3Key = generateS3Key(chatId, fileExtension);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .metadata(java.util.Map.of(
                    "original-filename", originalFilename,
                    "chat-id", String.valueOf(chatId)
                ))
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Uploaded file to S3: {}", s3Key);

            return s3Key;
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new InvalidFileUploadException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Generate a presigned URL for downloading a file
     */
    public String generatePresignedUrl(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlDuration))
                .getObjectRequest(getObjectRequest)
                .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", s3Key, e);
            return null;
        }
    }

    /**
     * Delete a file from S3
     */
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted file from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", s3Key, e);
        }
    }

    /**
     * Check if bucket exists, create if not
     */
    public void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 bucket exists: {}", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("Creating S3 bucket: {}", bucketName);
            createBucket();
        }
    }

    private void createBucket() {
        CreateBucketRequest request = CreateBucketRequest.builder()
            .bucket(bucketName)
            .build();

        s3Client.createBucket(request);
        log.info("S3 bucket created: {}", bucketName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileUploadException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileUploadException(
                "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + " MB",
                file.getOriginalFilename()
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new InvalidFileUploadException(
                "File type not allowed: " + contentType,
                file.getOriginalFilename()
            );
        }
    }

    private String generateS3Key(Long chatId, String fileExtension) {
        return String.format("chat-attachments/%d/%s%s",
            chatId,
            UUID.randomUUID().toString(),
            fileExtension
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
