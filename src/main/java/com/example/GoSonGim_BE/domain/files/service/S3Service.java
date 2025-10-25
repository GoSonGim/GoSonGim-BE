package com.example.GoSonGim_BE.domain.files.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 업로드용 Presigned URL 생성
    public URL generateUploadPresignedUrl(String folder, String fileName, int expirationMinutes, String userId) {
        String dateFolder = LocalDate.now().toString(); // 2025-10-09
        String uniqueFileName = userId + "_" + UUID.randomUUID() + "_" + fileName;
        String key = String.format("%s/%s/%s", folder, dateFolder, uniqueFileName);

        Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60));

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(request);
    }

    // 다운로드용 Presigned URL 생성
    public URL generateDownloadPresignedUrl(String fileKey, int expirationMinutes) {
        Date expiration = Date.from(Instant.now().plusSeconds(expirationMinutes * 60));

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(request);
    }
}