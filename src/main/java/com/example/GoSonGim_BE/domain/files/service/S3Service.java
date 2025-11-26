package com.example.GoSonGim_BE.domain.files.service;

import com.example.GoSonGim_BE.domain.files.exception.FilesExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.image-bucket}")
    private String imageBucketName;

    @Value("${aws.s3.region}")
    private String region;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String IMAGE_FOLDER = "situation";
    private static final String S3_URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";

    // 업로드용 Presigned URL 생성 (fileKey를 직접 받아서 사용)
    public URL generateUploadPresignedUrl(String fileKey, int expirationMinutes) {

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url();
    }

    // 다운로드용 Presigned URL 생성
    public URL generateDownloadPresignedUrl(String fileKey, int expirationMinutes) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url();
    }
    
    // S3에서 파일을 InputStream으로 다운로드
    public InputStream downloadFileAsStream(String fileKey) {
        try {
            log.info("Attempting to download file: {}", fileKey);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            InputStream stream = s3Client.getObject(getObjectRequest);
            log.info("Successfully downloaded file using S3Client: {}", fileKey);
            return stream;
        } catch (Exception e) {
            log.error("Failed to download file: {} - Error: {}", fileKey, e.getMessage(), e);
            throw new FilesExceptions.S3DownloadFailed(fileKey);
        }
    }

    // S3에서 파일 삭제
    public void deleteFile(String fileKey) {
        log.info("Attempting to delete file from S3: {}", fileKey);
        
        // 파일 존재 여부 확인
        if (!isFileExists(fileKey)) {
            log.warn("File does not exist in S3: {}", fileKey);
            throw new FilesExceptions.S3FileNotFound(fileKey);
        }
        
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted file from S3: {}", fileKey);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {} - Error: {}", fileKey, e.getMessage(), e);
            throw new FilesExceptions.S3DeleteFailed(fileKey);
        }
    }

    // 파일 존재 여부 확인
    private boolean isFileExists(String fileKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence: {} - Error: {}", fileKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 이미지 파일을 S3에 직접 업로드하고 공개 URL을 반환합니다.
     * 이미지는 항상 "situation" 폴더에 저장됩니다.
     * 
     * @param file 업로드할 이미지 파일
     * @return S3 공개 URL
     */
    public String uploadImage(MultipartFile file) {
        validateFileSize(file);

        try {
            String fileKey = generateFileKey();
            String contentType = file.getContentType() != null 
                    ? file.getContentType() 
                    : "application/octet-stream";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(imageBucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.debug("Successfully uploaded image to S3: {} - ETag: {}", fileKey, response.eTag());

            return generatePublicUrl(fileKey);

        } catch (FilesExceptions.FileSizeExceeded e) {
            throw e;
        } catch (IOException e) {
            log.error("IO error during image upload - Error: {}", e.getMessage(), e);
            throw new FilesExceptions.S3UploadFailed("이미지 파일 읽기 실패");
        } catch (Exception e) {
            log.error("Unexpected error during image upload - Error: {}", e.getMessage(), e);
            throw new FilesExceptions.S3UploadFailed("이미지 업로드 실패");
        }
    }

    /**
     * 파일 크기 검증
     */
    private void validateFileSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FilesExceptions.InvalidFileType("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new FilesExceptions.FileSizeExceeded(MAX_IMAGE_SIZE);
        }
    }

    /**
     * 파일 키 생성 (situation/날짜/UUID_타임스탬프)
     */
    private String generateFileKey() {
        String dateFolder = LocalDate.now().toString();
        String randomUUID = UUID.randomUUID().toString();
        String fileName = String.format("%s_%d", randomUUID, System.currentTimeMillis());
        return String.format("%s/%s/%s", IMAGE_FOLDER, dateFolder, fileName);
    }

    /**
     * S3 공개 URL 생성
     * 버킷이 public이면 이 URL로 직접 접근 가능합니다.
     * 버킷이 private이면 CloudFront나 다른 CDN을 사용하거나 버킷 정책을 수정해야 합니다.
     */
    private String generatePublicUrl(String fileKey) {
        return String.format(S3_URL_FORMAT, imageBucketName, region, fileKey);
    }
}