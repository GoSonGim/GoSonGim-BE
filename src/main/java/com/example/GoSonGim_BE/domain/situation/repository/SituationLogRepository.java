package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import com.example.GoSonGim_BE.domain.situation.entity.SituationLog;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface SituationLogRepository extends JpaRepository<SituationLog, Long> {
    
    /**
     * 사용자가 성공한 상황극 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 성공한 상황극 개수
     */
    @Query("SELECT COUNT(DISTINCT sl.situation.id) FROM SituationLog sl " +
           "WHERE sl.user.id = :userId AND sl.isSuccess = true")
    Long countDistinctSuccessfulSituationsByUserId(@Param("userId") Long userId);

    /**
     * 전체 카테고리에서 사용자의 최신 상황극 학습 기록 조회 (카테고리 필터 없음)
     */
    @Query("""
        SELECT sl
        FROM SituationLog sl
        WHERE sl.user.id = :userId
          AND sl.id = (
              SELECT MAX(sl2.id)
              FROM SituationLog sl2
              WHERE sl2.user.id = :userId
                AND sl2.situation.id = sl.situation.id
          )
        """)
    Slice<SituationLog> findLatestSituationLogsAllCategories(@Param("userId") Long userId,
                                                             Pageable pageable);

    /**
     * 특정 카테고리에서 사용자의 최신 상황극 학습 기록 조회
     */
    @Query("""
        SELECT sl
        FROM SituationLog sl
        WHERE sl.user.id = :userId
          AND sl.situation.situationCategory = :category
          AND sl.id = (
              SELECT MAX(sl2.id)
              FROM SituationLog sl2
              WHERE sl2.user.id = :userId
                AND sl2.situation.id = sl.situation.id
                AND sl2.situation.situationCategory = :category
          )
        """)
    Slice<SituationLog> findLatestSituationLogsByCategory(@Param("userId") Long userId,
                                                          @Param("category") SituationCategory category,
                                                          Pageable pageable);

    /**
     * 특정 월에 사용자가 학습한 날짜 목록 조회 (중복 제거)
     * 
     * @param userId 사용자 ID
     * @param year 연도
     * @param month 월 (1-12)
     * @return 학습이 있었던 날짜 목록 
     */
    @Query(value = """
        SELECT DISTINCT DATE(sl.created_at) as learning_date
        FROM situation_log sl
        WHERE sl.user_id = :userId
          AND YEAR(sl.created_at) = :year
          AND MONTH(sl.created_at) = :month
        ORDER BY learning_date ASC
        """, nativeQuery = true)
    List<Date> findDistinctLearningDatesByMonth(@Param("userId") Long userId,
                                                 @Param("year") int year,
                                                 @Param("month") int month);
}
