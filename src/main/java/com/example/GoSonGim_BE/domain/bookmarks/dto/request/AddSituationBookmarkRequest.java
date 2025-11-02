package com.example.GoSonGim_BE.domain.bookmarks.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddSituationBookmarkRequest(
    @NotNull(message = "상황극 목록은 필수입니다.")
    @Size(min = 1, message = "최소 1개의 상황극을 선택해야 합니다.")
    List<Long> situationList
) {
}