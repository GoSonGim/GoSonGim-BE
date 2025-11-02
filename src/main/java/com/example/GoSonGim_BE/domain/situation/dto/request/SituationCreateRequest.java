package com.example.GoSonGim_BE.domain.situation.dto.request;

import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 상황극 생성 요청 DTO
 */
public record SituationCreateRequest(
    @NotNull(message = "카테고리는 필수입니다.")
    SituationCategory situationCategory,

    @NotBlank(message = "상황극명은 필수입니다.")
    String situationName,

    @NotBlank(message = "설명은 필수입니다.")
    String description,

    String image
) {}