package com.example.GoSonGim_BE.global.constant;

/**
 * API 버전 관리 상수
 * 
 * @author GoSonGim Team
 * @since 1.0.0
 */
public final class ApiVersion {
    
    /**
     * API v1 버전 경로
     */
    public static final String V1 = "/api/v1";
    
    /**
     * API v2 버전 경로 (향후 확장용)
     */
    public static final String V2 = "/api/v2";
    
    /**
     * 현재 활성화된 API 버전
     */
    public static final String CURRENT = V1;
    
    /**
     * API 버전 경로를 반환
     * 
     * @param version 버전 번호
     * @return API 버전 경로
     */
    public static String getVersionPath(int version) {
        return "/api/v" + version;
    }
    
    /**
     * 생성자 숨김 (유틸리티 클래스)
     */
    private ApiVersion() {
        throw new UnsupportedOperationException("Utility class");
    }
}
