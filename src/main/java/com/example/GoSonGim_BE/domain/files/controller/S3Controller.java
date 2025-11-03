package com.example.GoSonGim_BE.domain.files.controller;

import com.example.GoSonGim_BE.domain.files.dto.response.S3PresignedUrlResponse;
import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "File API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/files")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 업로드용 Presigned URL 발급
    @Operation(summary = "업로드 URL 생성")
    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<S3PresignedUrlResponse>> generateUploadUrl(@RequestParam String folder,
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

    // 다운로드용 Presigned URL 발급
    @Operation(summary = "다운로드 URL 생성")
    @GetMapping("/download-url")
    public ResponseEntity<ApiResult<S3PresignedUrlResponse>> generateDownloadUrl(@RequestParam String fileKey) {
        URL downloadUrl = s3Service.generateDownloadPresignedUrl(fileKey, 60); //1시간 유효
        S3PresignedUrlResponse response = new S3PresignedUrlResponse(fileKey, downloadUrl.toString(), 60);
        ApiResult<S3PresignedUrlResponse> apiResult = ApiResult.success(200, "다운로드 URL이 생성되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }

    // 파일 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteFile(@RequestParam String fileKey) {
        s3Service.deleteFile(fileKey);
        ApiResponse<String> apiResponse = ApiResponse.success(200, "파일이 성공적으로 삭제되었습니다.", fileKey);
        return ResponseEntity.ok(apiResponse);
    }
}