package com.yusay.user.api.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 権限エンティティ
 * システムにおける権限（Permission）を表現
 */
@Table("permissions")
public class Permission {

    @Id
    private Long id;

    private String name;
    private String resource;
    private String action;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Permission() {
        // Spring Data JDBC用のデフォルトコンストラクタ
    }

    public Permission(String name, String resource, String action, String description) {
        this(name, resource, action, description, LocalDateTime.now());
    }

    // テスト用コンストラクタ - タイムスタンプを指定可能
    public Permission(String name, String resource, String action, String description, LocalDateTime timestamp) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
        this.createdAt = timestamp;
        this.updatedAt = timestamp;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Permission{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", resource='" + resource + '\'' +
               ", action='" + action + '\'' +
               ", description='" + description + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
