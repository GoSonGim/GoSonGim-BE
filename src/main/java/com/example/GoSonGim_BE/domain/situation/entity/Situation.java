package com.example.GoSonGim_BE.domain.situation.entity;

import com.example.GoSonGim_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * 상황극
 */
@Entity
@Table(name = "situation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Situation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("카테고리")
    @Column(name = "situation_category", nullable = false)
    private String situationCategory;

    @Comment("상황극명")
    @Column(name = "situation_name", nullable = false)
    private String situationName;

    @Comment("설명")
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Comment("이미지 URL")
    @Column(name = "image")
    private String image;

    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SituationLog> situationLogs = new ArrayList<>();
}