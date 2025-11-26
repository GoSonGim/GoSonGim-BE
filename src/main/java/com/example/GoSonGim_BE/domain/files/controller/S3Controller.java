package com.example.GoSonGim_BE.domain.files.controller;

import com.example.GoSonGim_BE.domain.files.dto.response.S3ImageUploadResponse;
import com.example.GoSonGim_BE.domain.files.dto.response.S3PresignedUrlResponse;
import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "File API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/files")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "Presigned URL 발급")
    @PostMapping("/upload-url")
    public ResponseEntity<ApiResult<S3PresignedUrlResponse>> generateUploadUrl(@RequestParam String folder,
                                                 @RequestParam String fileName) {

        // fileKey 생성 규칙 통일
        String dateFolder = LocalDate.now().toString();
        String randomUUID = UUID.randomUUID().toString();
        String fileKey = String.format("%s/%s/%s_%s", folder, dateFolder, randomUUID, fileName);
        
        URL uploadUrl = s3Service.generateUploadPresignedUrl(fileKey, 30); // 30분 유효

        S3PresignedUrlResponse response = new S3PresignedUrlResponse(fileKey, uploadUrl.toString(), 300);
        ApiResult<S3PresignedUrlResponse> apiResult = ApiResult.success(200, "업로드 URL이 생성되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }

    @Operation(summary = "다운로드 URL 발급")
    @GetMapping("/download-url")
    public ResponseEntity<ApiResult<S3PresignedUrlResponse>> generateDownloadUrl(@RequestParam String fileKey) {
        URL downloadUrl = s3Service.generateDownloadPresignedUrl(fileKey, 60); //1시간 유효
        S3PresignedUrlResponse response = new S3PresignedUrlResponse(fileKey, downloadUrl.toString(), 60);
        ApiResult<S3PresignedUrlResponse> apiResult = ApiResult.success(200, "다운로드 URL이 생성되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }

    @Operation(summary = "S3 파일 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResult<String>> deleteFile(@RequestParam String fileKey) {
        s3Service.deleteFile(fileKey);
        ApiResult<String> response = ApiResult.success(200, "파일이 성공적으로 삭제되었습니다.", fileKey);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이미지 파일 업로드")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<S3ImageUploadResponse>> uploadImage(
            @RequestPart("file") MultipartFile file) {
        
        String imageUrl = s3Service.uploadImage(file);
        
        S3ImageUploadResponse response = new S3ImageUploadResponse(imageUrl);
        ApiResult<S3ImageUploadResponse> apiResult = ApiResult.success(
                200, "이미지가 성공적으로 업로드되었습니다.", response);
        
        return ResponseEntity.ok(apiResult);
    }
}