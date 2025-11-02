package com.example.GoSonGim_BE.domain.kit.dto.response;

import java.util.List;

public record KitsResponse(
    int count,
    List<Kit> kits
) {
    public record Kit(
        Long kitId,
        String kitName
    ) {}
}
