package com.yusay.user.api.infrastructure;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.repository.UserRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final JdbcClient jdbcClient;

    public JdbcUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<User> findById(String id) {
        return jdbcClient.sql("""
                    SELECT id, username, email, password_hash, enabled,
                           account_non_expired, account_non_locked, credentials_non_expired,
                           created_at, updated_at
                    FROM users
                    WHERE id = :id
                """)
                .param("id", id)
                .query(User.class)
                .optional();
    }

    @Override
    public User save(User user) {
        // 既存のユーザーかどうかを確認
        Optional<User> existingUser = findById(user.id());
        
        if (existingUser.isPresent()) {
            // 更新処理
            jdbcClient.sql("""
                        UPDATE users
                        SET username = :username,
                            email = :email,
                            password_hash = :passwordHash,
                            enabled = :enabled,
                            account_non_expired = :accountNonExpired,
                            account_non_locked = :accountNonLocked,
                            credentials_non_expired = :credentialsNonExpired,
                            updated_at = :updatedAt
                        WHERE id = :id
                    """)
                    .param("id", user.id())
                    .param("username", user.username())
                    .param("email", user.email())
                    .param("passwordHash", user.passwordHash())
                    .param("enabled", user.enabled())
                    .param("accountNonExpired", user.accountNonExpired())
                    .param("accountNonLocked", user.accountNonLocked())
                    .param("credentialsNonExpired", user.credentialsNonExpired())
                    .param("updatedAt", LocalDateTime.now())
                    .update();
        } else {
            // 新規作成処理
            LocalDateTime now = LocalDateTime.now();
            jdbcClient.sql("""
                        INSERT INTO users (id, username, email, password_hash, enabled,
                                          account_non_expired, account_non_locked, credentials_non_expired,
                                          created_at, updated_at)
                        VALUES (:id, :username, :email, :passwordHash, :enabled,
                                :accountNonExpired, :accountNonLocked, :credentialsNonExpired,
                                :createdAt, :updatedAt)
                    """)
                    .param("id", user.id())
                    .param("username", user.username())
                    .param("email", user.email())
                    .param("passwordHash", user.passwordHash())
                    .param("enabled", user.enabled())
                    .param("accountNonExpired", user.accountNonExpired())
                    .param("accountNonLocked", user.accountNonLocked())
                    .param("credentialsNonExpired", user.credentialsNonExpired())
                    .param("createdAt", now)
                    .param("updatedAt", now)
                    .update();
        }
        
        // 保存後のユーザーを取得して返す
        return findById(user.id()).orElseThrow();
    }
}
