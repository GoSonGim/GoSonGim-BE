package com.example.GoSonGim_BE.domain.bookmarks.entity;

import com.example.GoSonGim_BE.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("사용자 참조")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Comment("북마크 대상 타입")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private BookmarkedTargetType targetType;

    @Comment("북마크 대상 ID")
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @CreatedDate
    @Comment("생성 일시")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}