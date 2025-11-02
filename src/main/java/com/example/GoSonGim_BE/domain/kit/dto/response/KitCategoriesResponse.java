package com.example.GoSonGim_BE.domain.kit.dto.response;

import java.util.List;

public record KitCategoriesResponse(
    int count,
    List<Category> categories
) {
    public record Category(
        Long categoryId,
        String categoryName
    ) {}
}