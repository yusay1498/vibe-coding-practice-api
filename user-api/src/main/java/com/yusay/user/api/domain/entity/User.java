package com.yusay.user.api.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
public record User(
    @Id
    String id,
    String username,
    String email,
    @JsonIgnore
    String passwordHash,
    Boolean enabled,
    Boolean accountNonExpired,
    Boolean accountNonLocked,
    Boolean credentialsNonExpired,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
