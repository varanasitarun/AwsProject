package com.example.fileupload.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.example.fileupload.model.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileController {

    @Value("${aws.s3.bucket}")
    private String bucketName;


    @Value("${aws.dynamodb.table}")
    private String tableName;

    private final AmazonS3 s3Client;
    private final AmazonDynamoDB dynamoDB;

    public FileController(AmazonS3 s3Client, AmazonDynamoDB dynamoDB) {
        this.s3Client = s3Client;
        this.dynamoDB = dynamoDB;
    }

    @GetMapping("/presigned-url")
    public Map<String, String> getPresignedUrl(@RequestParam String filename) {
        String key = "uploads/" + UUID.randomUUID() + "_" + filename;

        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10 mins


//        GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                new GeneratePresignedUrlRequest(bucketName, key)
//                        .withMethod(HttpMethod.GET)
//                        .withExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
//
//        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//        String presignedUrl = url.toString();
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(request);

        Map<String, String> response = new HashMap<>();
        System.out.println(">>>>>>"+response);
        response.put("url", url.toString());
        //response.put("url", presignedUrl);
        response.put("key", key);
        return response;
    }

//    @PostMapping("/metadata")
//    public ResponseEntity<String> saveMetadata(@RequestBody FileMetadata metadata) {
//        Map<String, AttributeValue> item = new HashMap<>();
//        item.put("FileId", new AttributeValue(UUID.randomUUID().toString()));
//        item.put("filename", new AttributeValue(metadata.getFilename()));
//        item.put("s3Url", new AttributeValue(metadata.getS3Url()));
//        item.put("uploadTime", new AttributeValue(metadata.getUploadTime()));
//
//        PutItemRequest putItemRequest = new PutItemRequest()
//                .withTableName(tableName)
//                .withItem(item);
//
//        dynamoDB.putItem(putItemRequest);
//
//        return ResponseEntity.ok("Metadata saved to DynamoDB");
//    }

//    @PostMapping("/metadata")
//    public ResponseEntity<String> saveMetadata(@RequestBody FileMetadata metadata) {
//        Map<String, AttributeValue> item = new HashMap<>();
//        item.put("FileId", new AttributeValue(UUID.randomUUID().toString()));
//        item.put("filename", new AttributeValue(metadata.getFilename()));
//        // Save the S3 key, NOT the presigned URL or public URL
//        item.put("s3Key", new AttributeValue(metadata.getS3Key()));
//        item.put("uploadTime", new AttributeValue(metadata.getUploadTime()));
//
//        PutItemRequest putItemRequest = new PutItemRequest()
//                .withTableName(tableName)
//                .withItem(item);
//
//        dynamoDB.putItem(putItemRequest);
//
//        return ResponseEntity.ok("Metadata saved to DynamoDB");
//    }

    @PostMapping("/metadata")
    public ResponseEntity<String> saveMetadata(@RequestBody FileMetadata metadata) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("FileId", new AttributeValue(UUID.randomUUID().toString()));

        if (metadata.getFilename() == null || metadata.getFilename().isEmpty()) {
            return ResponseEntity.badRequest().body("Filename is required");
        }
        item.put("filename", new AttributeValue(metadata.getFilename()));

        if (metadata.getS3Url() == null || metadata.getS3Url().isEmpty()) {
            return ResponseEntity.badRequest().body("S3 URL is required");
        }
        item.put("s3Url", new AttributeValue(metadata.getS3Url()));

        if (metadata.getUploadTime() == null || metadata.getUploadTime().isEmpty()) {
            // Optionally set current time if missing:
            item.put("uploadTime", new AttributeValue(Instant.now().toString()));
        } else {
            item.put("uploadTime", new AttributeValue(metadata.getUploadTime()));
        }

        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(tableName)
                .withItem(item);

        dynamoDB.putItem(putItemRequest);

        return ResponseEntity.ok("Metadata saved to DynamoDB");
    }

    @GetMapping("/download-url")
    public Map<String, String> getDownloadUrl(@RequestParam String key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10 minutes expiration

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(request);

        Map<String, String> response = new HashMap<>();
        response.put("url", url.toString());
        return response;
    }
}
