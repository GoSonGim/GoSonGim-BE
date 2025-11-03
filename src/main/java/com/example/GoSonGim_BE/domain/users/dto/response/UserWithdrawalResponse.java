package com.example.GoSonGim_BE.domain.users.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserWithdrawalResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purgeAt;

    public static UserWithdrawalResponse of(LocalDateTime deletedAt) {
        return UserWithdrawalResponse.builder()
                .deletedAt(deletedAt)
                .purgeAt(deletedAt.plusDays(30))
                .build();
    }
}