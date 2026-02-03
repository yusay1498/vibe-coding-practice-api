package com.yusay.user.api.presentation.dto;

import java.time.LocalDateTime;

/**
 * 全件削除レスポンス
 * 
 * @param success 成功フラグ
 * @param deletedCount 削除されたユーザー数
 * @param executedAt 削除実行日時
 * @param environment 実行環境
 * @param message メッセージ
 */
public record DeleteAllResponse(
    boolean success,
    int deletedCount,
    LocalDateTime executedAt,
    String environment,
    String message
) {
}
