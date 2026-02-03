package com.yusay.user.api.domain.exception;

/**
 * ユーザーの重複が発生した場合にスローされる例外
 */
public class DuplicateUserException extends RuntimeException {
    private final String fieldName;
    
    public DuplicateUserException(String fieldName) {
        super("ユーザーが既に存在します");
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
