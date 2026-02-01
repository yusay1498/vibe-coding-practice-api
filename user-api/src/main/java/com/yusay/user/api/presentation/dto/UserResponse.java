package com.yusay.user.api.presentation.dto;

import com.yusay.user.api.domain.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
    String id,
    String username,
    String email,
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean accountNonLocked,
    Boolean credentialsNonExpired,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.id(),
            user.username(),
            user.email(),
            user.enabled(),
            user.accountNonExpired(),
            user.accountNonLocked(),
            user.credentialsNonExpired(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
