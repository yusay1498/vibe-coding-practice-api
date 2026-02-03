package com.yusay.user.api.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * ユーザー更新リクエスト用のDTO
 * 
 * 各フィールドはnull許容で、nullの場合は既存値を保持する
 */
public record UpdateUserRequest(
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以内で入力してください")
    String username,
    
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
    String email,
    
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    String password,
    
    Boolean enabled,
    
    Boolean accountNonExpired,
    
    Boolean accountNonLocked,
    
    Boolean credentialsNonExpired
) {
    @Override
    public String toString() {
        return "UpdateUserRequest[" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + (password != null ? "****" : null) + '\'' +
                ", enabled=" + enabled +
                ", accountNonExpired=" + accountNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ']';
    }
}
