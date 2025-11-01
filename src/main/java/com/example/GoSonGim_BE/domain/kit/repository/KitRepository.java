package com.example.GoSonGim_BE.domain.kit.repository;

import com.example.GoSonGim_BE.domain.kit.entity.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitRepository extends JpaRepository<Kit, Long> {
    List<Kit> findByIdIn(List<Long> ids);
}