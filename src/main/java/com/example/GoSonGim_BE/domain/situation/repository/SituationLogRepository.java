package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.SituationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
