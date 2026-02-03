package com.yusay.user.api.presentation.constant;

/**
 * エラーメッセージ定数
 * クライアントに返却される安全なエラーメッセージを一元管理
 */
public final class ErrorMessages {
    
    /**
     * 全件削除が許可されていない場合のメッセージ
     * 内部情報（削除上限値など）を含まない汎用的なメッセージ
     */
    public static final String DELETE_ALL_NOT_ALLOWED = "全件削除は現在の環境またはデータ状態では実行できません";
    
    private ErrorMessages() {
        // ユーティリティクラスのためインスタンス化を禁止
        throw new AssertionError("ErrorMessagesはインスタンス化できません");
    }
}
