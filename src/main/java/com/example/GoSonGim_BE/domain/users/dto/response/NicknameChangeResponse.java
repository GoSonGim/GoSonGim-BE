package com.example.GoSonGim_BE.domain.users.dto.response;

public record NicknameChangeResponse(
    String nickname
) {
    public static NicknameChangeResponse of(String nickname) {
        return new NicknameChangeResponse(nickname);
    }
}