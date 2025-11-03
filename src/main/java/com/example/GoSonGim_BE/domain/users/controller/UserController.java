package com.example.GoSonGim_BE.domain.users.controller;

import com.example.GoSonGim_BE.domain.users.dto.request.NicknameChangeRequest;
import com.example.GoSonGim_BE.domain.users.dto.response.DailyWordsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.NicknameChangeResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserProfileResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserStudyStatisticsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserWithdrawalResponse;
import com.example.GoSonGim_BE.domain.users.service.UserService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiVersion.CURRENT + "/users/me")
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    /**
     * 내 프로필 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile() {
        //Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "내 프로필 조회에 성공하였습니다.", response));
    }

    /**
     * 내 학습 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStudyStatisticsResponse>> getUserStudyStatistics() {
        //Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        UserStudyStatisticsResponse response = userService.getUserStudyStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "내 학습 통계 조회 성공", response));
    }

    /**
     * 일별 학습 단어 목록 조회
     */
    @GetMapping("/stats/daily-words")
    public ResponseEntity<ApiResponse<DailyWordsResponse>> getDailyWords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        //Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        DailyWordsResponse response = userService.getDailyWords(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "학습 통계 상세 조회 성공", response));
    }

    /**
     * 닉네임 변경
     */
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<NicknameChangeResponse>> changeNickname(
            @Valid @RequestBody NicknameChangeRequest request) {
        //Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        NicknameChangeResponse response = userService.changeNickname(userId, request.getNickname());
        return ResponseEntity.ok(ApiResponse.success(200, "닉네임이 성공적으로 변경되었습니다.", response));
    }

    /**
     * 사용자 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<UserWithdrawalResponse>> deleteUser() {
        //Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        UserWithdrawalResponse response = userService.withdrawUser(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "탈퇴가 접수되었습니다. 30일 후 계정과 데이터가 영구 삭제됩니다.", response));
    }
}
