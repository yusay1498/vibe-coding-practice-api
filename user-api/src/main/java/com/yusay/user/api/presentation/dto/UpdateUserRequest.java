package com.yusay.user.api.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 3, max = 50, message = "ユーザー名は3〜50文字である必要があります")
    String username,
    
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 100, message = "メールアドレスは100文字以内である必要があります")
    String email,
    
    @Size(min = 8, max = 255, message = "パスワードは8〜255文字である必要があります")
    String password,
    
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean accountNonLocked,
    Boolean credentialsNonExpired
) {
}
