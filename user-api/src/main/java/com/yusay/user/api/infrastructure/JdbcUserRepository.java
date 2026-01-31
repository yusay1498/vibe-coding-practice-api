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
        LocalDateTime now = LocalDateTime.now();
        
        // PostgreSQLのON CONFLICTとRETURNINGを使用してupsert処理を実行
        return jdbcClient.sql("""
                    INSERT INTO users (id, username, email, password_hash, enabled,
                                      account_non_expired, account_non_locked, credentials_non_expired,
                                      created_at, updated_at)
                    VALUES (:id, :username, :email, :passwordHash, :enabled,
                            :accountNonExpired, :accountNonLocked, :credentialsNonExpired,
                            :createdAt, :updatedAt)
                    ON CONFLICT (id)
                    DO UPDATE SET
                        username = EXCLUDED.username,
                        email = EXCLUDED.email,
                        password_hash = EXCLUDED.password_hash,
                        enabled = EXCLUDED.enabled,
                        account_non_expired = EXCLUDED.account_non_expired,
                        account_non_locked = EXCLUDED.account_non_locked,
                        credentials_non_expired = EXCLUDED.credentials_non_expired,
                        updated_at = EXCLUDED.updated_at
                    RETURNING id, username, email, password_hash, enabled,
                              account_non_expired, account_non_locked, credentials_non_expired,
                              created_at, updated_at
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
                .query(User.class)
                .single();
    }
}
