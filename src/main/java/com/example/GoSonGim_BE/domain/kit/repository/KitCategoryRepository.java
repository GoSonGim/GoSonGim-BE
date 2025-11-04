package com.example.GoSonGim_BE.domain.kit.repository;

import com.example.GoSonGim_BE.domain.kit.entity.KitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitCategoryRepository extends JpaRepository<KitCategory, Long> {
}