package com.example.GoSonGim_BE.domain.users.controller;

import com.example.GoSonGim_BE.domain.users.dto.request.NicknameChangeRequest;
import com.example.GoSonGim_BE.domain.users.dto.response.ContinuousDaysResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.DailyWordsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.NicknameChangeResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserProfileResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserStudyStatisticsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserWithdrawalResponse;
import com.example.GoSonGim_BE.domain.users.service.UserService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping
    public ResponseEntity<ApiResult<UserProfileResponse>> getUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResult.success(200, "내 프로필 조회에 성공하였습니다.", response));
    }

    @Operation(summary = "내 학습 통계 조회")
    @GetMapping("/stats")
    public ResponseEntity<ApiResult<UserStudyStatisticsResponse>> getUserStudyStatistics(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserStudyStatisticsResponse response = userService.getUserStudyStatistics(userId);
        return ResponseEntity.ok(ApiResult.success(200, "내 학습 통계 조회 성공", response));
    }

    @Operation(summary = "날짜별 학습 단어 조회")
    @GetMapping("/stats/daily-words")
    public ResponseEntity<ApiResult<DailyWordsResponse>> getDailyWords(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = (Long) authentication.getPrincipal();
        DailyWordsResponse response = userService.getDailyWords(userId, page, size);
        return ResponseEntity.ok(ApiResult.success(200, "성공 누적 그래프 조회 성공", response));
    }

    @Operation(summary = "닉네임 변경")
    @PutMapping("/nickname")
    public ResponseEntity<ApiResult<NicknameChangeResponse>> changeNickname(
            Authentication authentication,
            @Valid @RequestBody NicknameChangeRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        NicknameChangeResponse response = userService.changeNickname(userId, request.getNickname());
        return ResponseEntity.ok(ApiResult.success(200, "닉네임이 성공적으로 변경되었습니다.", response));
    }

    @Operation(summary = "사용자 탈퇴")
    @DeleteMapping
    public ResponseEntity<ApiResult<UserWithdrawalResponse>> deleteUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserWithdrawalResponse response = userService.withdrawUser(userId);
        return ResponseEntity.ok(ApiResult.success(200, "탈퇴가 접수되었습니다. 30일 후 계정과 데이터가 영구 삭제됩니다.", response));
    }

    @Operation(summary = "연속 학습일 조회")
    @GetMapping("/streak-days")
    public ResponseEntity<ApiResult<ContinuousDaysResponse>> getContinuousDays(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ContinuousDaysResponse response = userService.getContinuousDays(userId);
        return ResponseEntity.ok(ApiResult.success(200, "연속 학습일 조회에 성공하였습니다.", response));
    }
}
