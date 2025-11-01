package com.example.GoSonGim_BE.domain.bookmarks.service;

import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddKitBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddSituationBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkPreviewListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkPreviewResponse;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkResponse;
import com.example.GoSonGim_BE.domain.bookmarks.entity.Bookmark;
import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;
import com.example.GoSonGim_BE.domain.bookmarks.exception.BookmarkExceptions;
import com.example.GoSonGim_BE.domain.bookmarks.repository.BookmarkRepository;
import com.example.GoSonGim_BE.domain.kit.entity.Kit;
import com.example.GoSonGim_BE.domain.kit.repository.KitRepository;
import com.example.GoSonGim_BE.domain.situation.entity.Situation;
import com.example.GoSonGim_BE.domain.situation.repository.SituationRepository;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {
    
    private final BookmarkRepository bookmarkRepository;
    private final KitRepository kitRepository;
    private final SituationRepository situationRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void addKitBookmarks(Long userId, AddKitBookmarkRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(BookmarkExceptions.UserNotFoundException::new);
        
        List<Kit> kits = kitRepository.findByIdIn(request.kitList());
        if (kits.size() != request.kitList().size()) {
            throw new BookmarkExceptions.KitNotFoundException();
        }
        
        List<Bookmark> bookmarksToSave = new ArrayList<>();
        for (Long kitId : request.kitList()) {
            if (!bookmarkRepository.existsByUserIdAndTargetTypeAndTargetId(userId, BookmarkedTargetType.KIT, kitId)) {
                Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .targetType(BookmarkedTargetType.KIT)
                    .targetId(kitId)
                    .build();
                bookmarksToSave.add(bookmark);
            }
        }
        
        if (!bookmarksToSave.isEmpty()) {
            bookmarkRepository.saveAll(bookmarksToSave);
        }
    }
    
    @Override
    @Transactional
    public void addSituationBookmarks(Long userId, AddSituationBookmarkRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(BookmarkExceptions.UserNotFoundException::new);
        
        List<Situation> situations = situationRepository.findByIdIn(request.situationList());
        if (situations.size() != request.situationList().size()) {
            throw new BookmarkExceptions.SituationNotFoundException();
        }
        
        List<Bookmark> bookmarksToSave = new ArrayList<>();
        for (Long situationId : request.situationList()) {
            if (!bookmarkRepository.existsByUserIdAndTargetTypeAndTargetId(userId, BookmarkedTargetType.SITUATION, situationId)) {
                Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .targetType(BookmarkedTargetType.SITUATION)
                    .targetId(situationId)
                    .build();
                bookmarksToSave.add(bookmark);
            }
        }
        
        if (!bookmarksToSave.isEmpty()) {
            bookmarkRepository.saveAll(bookmarksToSave);
        }
    }
    
    @Override
    @Transactional
    public void deleteBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(BookmarkExceptions.BookmarkNotFoundException::new);
        
        bookmarkRepository.delete(bookmark);
    }
    
    @Override
    public BookmarkListResponse getBookmarks(Long userId, BookmarkedTargetType type, String category, String sort) {
        if (type != BookmarkedTargetType.KIT && type != BookmarkedTargetType.SITUATION) {
            throw new BookmarkExceptions.InvalidBookmarkTypeException();
        }
        
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndTargetTypeOrderByCreatedAtDesc(userId, type);
        
        List<BookmarkResponse> bookmarkResponses = new ArrayList<>();
        
        if (type == BookmarkedTargetType.KIT) {
            List<Long> kitIds = bookmarks.stream().map(Bookmark::getTargetId).collect(Collectors.toList());
            List<Kit> kits = kitRepository.findByIdIn(kitIds);
            Map<Long, Kit> kitMap = kits.stream().collect(Collectors.toMap(Kit::getId, kit -> kit));
            
            for (Bookmark bookmark : bookmarks) {
                Kit kit = kitMap.get(bookmark.getTargetId());
                if (kit != null && (category == null || kit.getKitCategory().getKitCategoryName().equals(category))) {
                    bookmarkResponses.add(new BookmarkResponse(
                        bookmark.getId(),
                        kit.getId(),
                        kit.getKitName(),
                        kit.getKitCategory().getKitCategoryName(),
                        bookmark.getCreatedAt()
                    ));
                }
            }
        } else if (type == BookmarkedTargetType.SITUATION) {
            List<Long> situationIds = bookmarks.stream().map(Bookmark::getTargetId).collect(Collectors.toList());
            List<Situation> situations = situationRepository.findByIdIn(situationIds);
            Map<Long, Situation> situationMap = situations.stream().collect(Collectors.toMap(Situation::getId, situation -> situation));
            
            for (Bookmark bookmark : bookmarks) {
                Situation situation = situationMap.get(bookmark.getTargetId());
                if (situation != null && (category == null || situation.getSituationCategory().equals(category))) {
                    bookmarkResponses.add(new BookmarkResponse(
                        bookmark.getId(),
                        situation.getId(),
                        situation.getSituationName(),
                        situation.getSituationCategory(),
                        bookmark.getCreatedAt()
                    ));
                }
            }
        }
        
        return new BookmarkListResponse(
            type,
            sort != null ? sort : "latest",
            bookmarkResponses.size(),
            bookmarkResponses
        );
    }
    
    @Override
    public BookmarkPreviewListResponse getBookmarkPreview(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<BookmarkPreviewResponse> previews = new ArrayList<>();
        
        for (Bookmark bookmark : bookmarks) {
            String title = "";
            String category = "";
            
            if (bookmark.getTargetType() == BookmarkedTargetType.KIT) {
                Kit kit = kitRepository.findById(bookmark.getTargetId()).orElse(null);
                if (kit != null) {
                    title = kit.getKitName();
                    category = kit.getKitCategory().getKitCategoryName();
                }
            } else if (bookmark.getTargetType() == BookmarkedTargetType.SITUATION) {
                Situation situation = situationRepository.findById(bookmark.getTargetId()).orElse(null);
                if (situation != null) {
                    title = situation.getSituationName();
                    category = situation.getSituationCategory();
                }
            }
            
            if (!title.isEmpty()) {
                previews.add(new BookmarkPreviewResponse(
                    bookmark.getId(),
                    bookmark.getTargetType(),
                    title,
                    category,
                    bookmark.getCreatedAt()
                ));
            }
        }
        
        return new BookmarkPreviewListResponse(previews.size(), previews);
    }
}