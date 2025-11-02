package com.example.GoSonGim_BE.domain.kit.repository;

import com.example.GoSonGim_BE.domain.kit.entity.Kit;
import com.example.GoSonGim_BE.domain.kit.entity.KitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitRepository extends JpaRepository<Kit, Long> {
    
    List<Kit> findByKitCategory(KitCategory kitCategory);
    
    List<Kit> findByKitCategoryId(Long categoryId);
}