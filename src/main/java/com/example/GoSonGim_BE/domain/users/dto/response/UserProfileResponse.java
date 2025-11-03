package com.example.GoSonGim_BE.domain.users.dto.response;

public record UserProfileResponse(
    UserInfo user
) {
    public record UserInfo(
        Long id,
        String nickname,
        String level
    ) {}
    
    public static UserProfileResponse from(Long id, String nickname, String level) {
        return new UserProfileResponse(
            new UserInfo(id, nickname, level)
        );
    }
}