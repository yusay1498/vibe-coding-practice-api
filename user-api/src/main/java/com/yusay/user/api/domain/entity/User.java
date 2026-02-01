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
    /**
     * 新規ユーザーを作成する際に作成日時を自動的に設定するファクトリメソッド
     * 
     * @param id ユーザーID（nullの場合はリポジトリ層で生成される）
     * @param username ユーザー名
     * @param email メールアドレス
     * @param passwordHash パスワードハッシュ
     * @param enabled 有効フラグ
     * @param accountNonExpired アカウント有効期限切れフラグ
     * @param accountNonLocked アカウントロックフラグ
     * @param credentialsNonExpired 認証情報有効期限切れフラグ
     * @return 作成日時と更新日時が設定された新規Userインスタンス
     */
    public static User create(
            String id,
            String username,
            String email,
            String passwordHash,
            Boolean enabled,
            Boolean accountNonExpired,
            Boolean accountNonLocked,
            Boolean credentialsNonExpired
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
                id,
                username,
                email,
                passwordHash,
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired,
                now,  // createdAt
                now   // updatedAt
        );
    }

    /**
     * 既存ユーザーを更新する際に更新日時を自動的に設定するメソッド
     * 
     * @param username 新しいユーザー名（nullの場合は既存値を保持）
     * @param email 新しいメールアドレス（nullの場合は既存値を保持）
     * @param passwordHash 新しいパスワードハッシュ（nullの場合は既存値を保持）
     * @param enabled 新しい有効フラグ（nullの場合は既存値を保持）
     * @param accountNonExpired 新しいアカウント有効期限切れフラグ（nullの場合は既存値を保持）
     * @param accountNonLocked 新しいアカウントロックフラグ（nullの場合は既存値を保持）
     * @param credentialsNonExpired 新しい認証情報有効期限切れフラグ（nullの場合は既存値を保持）
     * @return 更新日時が設定された更新後のUserインスタンス
     */
    public User update(
            String username,
            String email,
            String passwordHash,
            Boolean enabled,
            Boolean accountNonExpired,
            Boolean accountNonLocked,
            Boolean credentialsNonExpired
    ) {
        return new User(
                this.id,
                username != null ? username : this.username,
                email != null ? email : this.email,
                passwordHash != null ? passwordHash : this.passwordHash,
                enabled != null ? enabled : this.enabled,
                accountNonExpired != null ? accountNonExpired : this.accountNonExpired,
                accountNonLocked != null ? accountNonLocked : this.accountNonLocked,
                credentialsNonExpired != null ? credentialsNonExpired : this.credentialsNonExpired,
                this.createdAt,  // 作成日時は保持
                LocalDateTime.now()  // 更新日時を現在時刻に設定
        );
    }
}
