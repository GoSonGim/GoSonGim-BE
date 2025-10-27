package com.example.GoSonGim_BE.domain.kit.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class KitExceptions {

    public static Object KitStageNotFound;

    public static class UserNotFound extends BaseException {

        public UserNotFound() {
            super("존재하지 않는 유저입니다.", "USER_NOT_FOUND");
        }
    }

    public static class KitNotFound extends BaseException {

        public KitNotFound() {
            super("존재하지 않는 키트입니다.", "KIT_NOT_FOUND");
        }
    }

    public static class KitStageNotFound extends BaseException {

        public KitStageNotFound() {
            super("존재하지 않는 키트 단계입니다.", "Kit_Stage_NOT_FOUND");
        }
    }
}
