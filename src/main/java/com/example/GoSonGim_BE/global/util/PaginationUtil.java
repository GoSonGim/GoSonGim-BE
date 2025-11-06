package com.example.GoSonGim_BE.global.util;

import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

/**
 * 페이지네이션 유틸리티
 */
public final class PaginationUtil {

    private PaginationUtil() {
    }

    /**
     * 페이지/사이즈 파라미터를 검증하고 Pageable 을 생성합니다.
     *
     * @param page 요청 페이지 (1부터 시작)
     * @param size 페이지 크기
     * @param maxSize 허용되는 최대 페이지 크기
     * @param sort 정렬 정보
     * @param exceptionProvider 검증 실패 시 발생시킬 예외 생성 함수
     * @return Pageable 인스턴스
     */
    public static Pageable createPageable(int page, int size, int maxSize, Sort sort,
                                          Function<String, ? extends RuntimeException> exceptionProvider) {
        if (exceptionProvider == null) {
            throw new IllegalArgumentException("exceptionProvider는 null일 수 없습니다.");
        }

        if (page < 1) {
            throw exceptionProvider.apply("page");
        }
        if (size < 1 || size > maxSize) {
            throw exceptionProvider.apply("size");
        }

        return PageRequest.of(page - 1, size, sort);
    }

    /**
     * Slice 객체로부터 hasNext 여부를 계산합니다.
     */
    public static boolean hasNext(Slice<?> slice) {
        return slice != null && slice.hasNext();
    }
}


