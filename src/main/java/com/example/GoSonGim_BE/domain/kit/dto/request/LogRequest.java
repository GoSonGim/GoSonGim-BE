package com.example.GoSonGim_BE.domain.kit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogRequest(
    @NotNull(message = "kitStageId는 필수입니다.")
    Long kitStageId,
    
    @NotNull(message = "evaluationScore는 필수입니다.")
    Double evaluationScore,
    
    @NotBlank(message = "evaluationFeedback는 필수입니다.")
    String evaluationFeedback,
    
    @NotNull(message = "isSuccess는 필수입니다.")
    Boolean isSuccess,
    
    @NotBlank(message = "fileKey는 필수입니다.")
    String fileKey
) {
}