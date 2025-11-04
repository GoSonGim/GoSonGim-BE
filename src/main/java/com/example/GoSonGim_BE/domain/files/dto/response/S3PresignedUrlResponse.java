package com.example.GoSonGim_BE.domain.files.dto.response;

public record S3PresignedUrlResponse(
        String fileKey,
        String url,
        int expiresIn
) {}
