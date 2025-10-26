package com.example.GoSonGim_BE.domain.kit.entity;

import com.example.GoSonGim_BE.domain.kit_stage.entity.KitStage;
import com.example.GoSonGim_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * 조음 키트
 */
@Entity
@Table(name = "kit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Kit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("키트 카테고리")
    @Column(name = "kit_category", nullable = false)
    private String kitCategory;

    @Comment("키트명")
    @Column(name = "kit_name", nullable = false)
    private String kitName;

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL)
    @Builder.Default
    private List<KitStage> kitStages = new ArrayList<>();
}