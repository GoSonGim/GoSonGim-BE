package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.SituationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SituationLogRepository extends JpaRepository<SituationLog, Long> {
}

