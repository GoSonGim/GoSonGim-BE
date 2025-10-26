package com.example.GoSonGim_BE.domain.files.controller;

import com.example.GoSonGim_BE.domain.files.dto.response.S3PresignedUrlResponse;
import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(ApiVersion.CURRENT + "/files")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 업로드용 Presigned URL 발급
    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<S3PresignedUrlResponse>> generateUploadUrl(@RequestParam String folder,
                                                 @RequestParam String fileName,
                                                 @RequestParam String userId) {

        URL uploadUrl = s3Service.generateUploadPresignedUrl(folder, fileName, 5, userId); // 5분 유효

        // fileKey 생성 규칙 통일
        String dateFolder = LocalDate.now().toString();
        String randomUUID = UUID.randomUUID().toString();
        String fileKey = String.format("%s/%s/%s_%s_%s", folder, dateFolder, userId, randomUUID, fileName);

        S3PresignedUrlResponse response = new S3PresignedUrlResponse(fileKey, uploadUrl.toString(), 300);
        ApiResponse<S3PresignedUrlResponse> apiResponse = ApiResponse.success(200, "업로드 URL이 생성되었습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }

    // 다운로드용 Presigned URL 발급
    @GetMapping("/download-url")
    public ResponseEntity<ApiResponse<S3PresignedUrlResponse>> generateDownloadUrl(@RequestParam String fileKey) {
        URL downloadUrl = s3Service.generateDownloadPresignedUrl(fileKey, 60); //1시간 유효
        S3PresignedUrlResponse response = new S3PresignedUrlResponse(fileKey, downloadUrl.toString(), 60);
        ApiResponse<S3PresignedUrlResponse> apiResponse = ApiResponse.success(200, "다운로드 URL이 생성되었습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }
}