package com.yusay.user.api.presentation.constant;

/**
 * HTTPヘッダー定数
 * カスタムHTTPヘッダー名を一元管理
 */
public final class HttpHeaders {
    
    /**
     * 全件削除確認ヘッダー
     */
    public static final String CONFIRM_DELETE_ALL = "X-Confirm-Delete-All";
    
    private HttpHeaders() {
        // ユーティリティクラスのためインスタンス化を禁止
        throw new AssertionError("HttpHeadersはインスタンス化できません");
    }
}
