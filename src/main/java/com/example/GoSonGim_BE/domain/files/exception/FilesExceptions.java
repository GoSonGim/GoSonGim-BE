package com.example.GoSonGim_BE.domain.files.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class FilesExceptions {
    
    public static class S3UploadFailed extends BaseException {
        
        public S3UploadFailed(String fileKey) {
            super("S3 파일 업로드 실패: " + fileKey, "S3_UPLOAD_FAILED");
        }
    }
    
    public static class S3DownloadFailed extends BaseException {
        
        public S3DownloadFailed(String fileKey) {
            super("S3 파일 다운로드 실패: " + fileKey, "S3_DOWNLOAD_FAILED");
        }
    }
    
    public static class InvalidFileType extends BaseException {
        
        public InvalidFileType(String fileType) {
            super("지원하지 않는 파일 형식입니다: " + fileType, "INVALID_FILE_TYPE");
        }
    }
    
    public static class FileSizeExceeded extends BaseException {
        
        public FileSizeExceeded(long maxSize) {
            super("파일 크기가 제한을 초과했습니다. 최대: " + maxSize + " bytes", "FILE_SIZE_EXCEEDED");
        }
    }
    
    public static class S3DeleteFailed extends BaseException {
        
        public S3DeleteFailed(String fileKey) {
            super("S3 파일 삭제 실패: " + fileKey, "S3_DELETE_FAILED");
        }
    }
    
    public static class S3FileNotFound extends BaseException {
        
        public S3FileNotFound(String fileKey) {
            super("S3에 파일이 존재하지 않습니다: " + fileKey, "S3_FILE_NOT_FOUND");
        }
    }
}