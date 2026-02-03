package com.yusay.user.api.application.dto;

import java.time.LocalDateTime;

/**
 * 全件削除の実行結果
 * 
 * @param deletedCount 削除されたユーザー数
 * @param executedAt 削除実行日時
 * @param environment 実行環境
 */
public record DeleteAllResult(
    int deletedCount,
    LocalDateTime executedAt,
    String environment
) {
}
