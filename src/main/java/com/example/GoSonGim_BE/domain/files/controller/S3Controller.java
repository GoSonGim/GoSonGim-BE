package com.example.GoSonGim_BE.domain.files.controller;

import com.example.GoSonGim_BE.domain.files.dto.response.S3PresignedUrlResponse;
import com.example.GoSonGim_BE.domain.files.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 업로드용 Presigned URL 발급
    @PostMapping("/upload-url")
    public S3PresignedUrlResponse generateUploadUrl(@RequestParam String folder,
                                                 @RequestParam String fileName,
                                                 @RequestParam String userId) {

        URL uploadUrl = s3Service.generateUploadPresignedUrl(folder, fileName, 5, userId); // 5분 유효

        // fileKey 생성 규칙 통일
        String dateFolder = LocalDate.now().toString();
        String randomUUID = UUID.randomUUID().toString();
        String fileKey = String.format("%s/%s/%s_%s_%s", folder, dateFolder, userId, randomUUID, fileName);

        return new S3PresignedUrlResponse(fileKey, uploadUrl.toString(), 300);
    }

    // 다운로드용 Presigned URL 발급
    @GetMapping("/download-url")
    public S3PresignedUrlResponse generateDownloadUrl(@RequestParam String fileKey) {
        URL downloadUrl = s3Service.generateDownloadPresignedUrl(fileKey, 60); //1시간 유효
        return new S3PresignedUrlResponse(fileKey, downloadUrl.toString(), 60);
    }
}