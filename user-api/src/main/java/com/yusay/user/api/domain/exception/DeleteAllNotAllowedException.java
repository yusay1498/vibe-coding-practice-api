package com.yusay.user.api.domain.exception;

/**
 * 全件削除が許可されていない場合にスローされる例外
 */
public class DeleteAllNotAllowedException extends RuntimeException {
    public DeleteAllNotAllowedException(String message) {
        super(message);
    }
}
