package com.example.GoSonGim_BE.domain.kit.entity;

import com.example.GoSonGim_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 키트 카테고리
 */
@Entity
@Table(name = "kit_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KitCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("키트 카테고리명")
    @Column(name = "kit_category_name", nullable = false)
    private String kitCategoryName;
}
