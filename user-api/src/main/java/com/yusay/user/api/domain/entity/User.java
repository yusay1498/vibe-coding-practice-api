package com.yusay.user.api.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
public record User(
    @Id
    String id,
    String username,
    String email,
    String passwordHash,
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean accountNonLocked,
    Boolean credentialsNonExpired,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
