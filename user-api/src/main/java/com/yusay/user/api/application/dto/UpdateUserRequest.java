package com.yusay.user.api.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * ユーザー更新リクエスト
 * 
 * @param username ユーザー名（nullの場合は更新しない）
 * @param email メールアドレス（nullの場合は更新しない）
 * @param passwordHash パスワードハッシュ（nullの場合は更新しない）
 * @param enabled 有効フラグ（nullの場合は更新しない）
 * @param accountNonExpired アカウント有効期限切れフラグ（nullの場合は更新しない）
 * @param accountNonLocked アカウントロックフラグ（nullの場合は更新しない）
 * @param credentialsNonExpired 認証情報有効期限切れフラグ（nullの場合は更新しない）
 */
public record UpdateUserRequest(
    @Size(min = 1, max = 50, message = "ユーザー名は1文字以上50文字以下である必要があります")
    String username,
    
    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 100, message = "メールアドレスは100文字以下である必要があります")
    String email,
    
    @Size(max = 255, message = "パスワードハッシュは255文字以下である必要があります")
    String passwordHash,
    
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean accountNonLocked,
    Boolean credentialsNonExpired
) {
}
