package com.example.GoSonGim_BE.domain.bookmarks.repository;

import com.example.GoSonGim_BE.domain.bookmarks.entity.Bookmark;
import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, BookmarkedTargetType targetType, Long targetId);
    
    Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(Long userId, BookmarkedTargetType targetType, Long targetId);
    
    List<Bookmark> findByUserIdAndTargetTypeOrderByCreatedAtDesc(Long userId, BookmarkedTargetType targetType);
    
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.targetType = :targetType ORDER BY b.createdAt DESC")
    List<Bookmark> findBookmarksByUserIdAndTargetType(@Param("userId") Long userId, @Param("targetType") BookmarkedTargetType targetType, Pageable pageable);
    
    long countByUserIdAndTargetType(Long userId, BookmarkedTargetType targetType);
}