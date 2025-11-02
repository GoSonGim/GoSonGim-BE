package com.example.GoSonGim_BE.domain.bookmarks.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class BookmarkExceptions {
    
    /**
     * 사용자를 찾을 수 없는 예외
     */
    public static class UserNotFoundException extends BaseException {
        public UserNotFoundException() {
            super("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND");
        }
    }
    
    /**
     * 존재하지 않는 키트 예외
     */
    public static class KitNotFoundException extends BaseException {
        public KitNotFoundException() {
            super("존재하지 않는 키트가 포함되어 있습니다.", "KIT_NOT_FOUND");
        }
    }
    
    /**
     * 존재하지 않는 상황극 예외
     */
    public static class SituationNotFoundException extends BaseException {
        public SituationNotFoundException() {
            super("존재하지 않는 상황극이 포함되어 있습니다.", "SITUATION_NOT_FOUND");
        }
    }
    
    /**
     * 북마크를 찾을 수 없는 예외
     */
    public static class BookmarkNotFoundException extends BaseException {
        public BookmarkNotFoundException() {
            super("북마크를 찾을 수 없습니다.", "BOOKMARK_NOT_FOUND");
        }
    }
    
    /**
     * 잘못된 북마크 타입 예외
     */
    public static class InvalidBookmarkTypeException extends BaseException {
        public InvalidBookmarkTypeException() {
            super("지원하지 않는 북마크 타입입니다.", "INVALID_BOOKMARK_TYPE");
        }
    }
}