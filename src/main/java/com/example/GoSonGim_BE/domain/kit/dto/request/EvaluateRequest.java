package com.example.GoSonGim_BE.domain.kit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvaluateRequest(
    @NotNull(message = "kitStageId는 필수입니다.")
    Long kitStageId,
    
    @NotBlank(message = "fileKey는 필수입니다.")
    String fileKey,
    
    @NotBlank(message = "targetWord는 필수입니다.")
    String targetWord
) {
}