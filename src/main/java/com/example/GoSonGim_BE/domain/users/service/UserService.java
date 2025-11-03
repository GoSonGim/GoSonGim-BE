package com.example.GoSonGim_BE.domain.users.service;

import com.example.GoSonGim_BE.domain.users.dto.response.DailyWordsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.NicknameChangeResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserProfileResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserStudyStatisticsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserWithdrawalResponse;
import com.example.GoSonGim_BE.domain.users.entity.User;

public interface UserService {
    /**
     * 기본 사용자 생성
     */
    User createDefaultUser();

    /**
     * 사용자 조회
     */
    User findById(Long userId);
    
    /**
     * 사용자 프로필 조회
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 사용자 탈퇴
     */
    UserWithdrawalResponse withdrawUser(Long userId);
    
    /**
     * 닉네임 변경
     */
    NicknameChangeResponse changeNickname(Long userId, String nickname);
    
    /**
     * 사용자 레벨 업데이트
     */
    void updateUserLevel(Long userId);
    
    /**
     * 사용자 학습 통계 조회
     */
    UserStudyStatisticsResponse getUserStudyStatistics(Long userId);
    
    /**
     * 일별 학습 단어 목록 조회
     */
    DailyWordsResponse getDailyWords(Long userId, int page, int size);
}
