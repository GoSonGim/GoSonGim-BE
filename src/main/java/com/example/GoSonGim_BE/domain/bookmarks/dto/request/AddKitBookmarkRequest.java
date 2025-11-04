package com.example.GoSonGim_BE.domain.bookmarks.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddKitBookmarkRequest(
    @NotNull(message = "키트 목록은 필수입니다.")
    @Size(min = 1, message = "최소 1개의 키트를 선택해야 합니다.")
    List<Long> kitList
) {
}