package com.yusay.user.api.domain.exception;

/**
 * ユーザーの重複が発生した場合にスローされる例外
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String identifier) {
        super("ユーザーが既に存在します: " + (identifier != null ? identifier : ""));
    }
}
