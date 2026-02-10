package com.yusay.user.api.infrastructure;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.repository.UserRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserRepository implements UserRepository {
    
    // SQL定数: メンテナンス性向上とSQLインジェクション対策
    private static final String SELECT_ALL_COLUMNS = """
        SELECT id, username, email, password_hash, enabled,
               account_non_expired, account_non_locked, credentials_non_expired,
               created_at, updated_at
        """;
    
    private static final String FROM_USERS = "FROM users";
    
    private static final String SQL_FIND_ALL = SELECT_ALL_COLUMNS + FROM_USERS;
    
    private static final String SQL_FIND_BY_ID = SELECT_ALL_COLUMNS + FROM_USERS + " WHERE id = :id";
    
    private static final String SQL_FIND_BY_EMAIL = SELECT_ALL_COLUMNS + FROM_USERS + " WHERE email = :email";
    
    private static final String SQL_FIND_BY_USERNAME = SELECT_ALL_COLUMNS + FROM_USERS + " WHERE username = :username";
    
    private static final String SQL_UPDATE = """
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
        """;
    
    private static final String SQL_INSERT = """
        INSERT INTO users (id, username, email, password_hash, enabled,
                          account_non_expired, account_non_locked, credentials_non_expired,
                          created_at, updated_at)
        VALUES (:id, :username, :email, :passwordHash, :enabled,
                :accountNonExpired, :accountNonLocked, :credentialsNonExpired,
                :createdAt, :updatedAt)
        """;
    
    private static final String SQL_DELETE_BY_ID = "DELETE FROM users WHERE id = :id";
    
    private static final String SQL_DELETE_ALL = "DELETE FROM users";
    
    private final JdbcClient jdbcClient;

    public JdbcUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
  
    @Override
    public List<User> findAll() {
        return jdbcClient.sql(SQL_FIND_ALL)
                .query(User.class)
                .list();
    }

    @Override
    public Optional<User> findById(String id) {
        return jdbcClient.sql(SQL_FIND_BY_ID)
                .param("id", id)
                .query(User.class)
                .optional();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbcClient.sql(SQL_FIND_BY_EMAIL)
                .param("email", email)
                .query(User.class)
                .optional();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbcClient.sql(SQL_FIND_BY_USERNAME)
                .param("username", username)
                .query(User.class)
                .optional();
    }

    @Override
    public User save(User user) {
        // IDが指定されているかチェック
        String userId = user.id();
        Optional<User> existingUser;
        
        if (userId == null || userId.isBlank()) {
            // IDが未指定の場合は新規ユーザーとして扱う
            existingUser = Optional.empty();
            userId = UUID.randomUUID().toString();
        } else {
            // データ保証のため、既存ユーザーかどうかを確認
            existingUser = findById(userId);
        }
        
        if (existingUser.isPresent()) {
            // 既存ユーザーの場合はUPDATE
            jdbcClient.sql(SQL_UPDATE)
                    .param("id", user.id())
                    .param("username", user.username())
                    .param("email", user.email())
                    .param("passwordHash", user.passwordHash())
                    .param("enabled", user.enabled())
                    .param("accountNonExpired", user.accountNonExpired())
                    .param("accountNonLocked", user.accountNonLocked())
                    .param("credentialsNonExpired", user.credentialsNonExpired())
                    .param("updatedAt", user.updatedAt())
                    .update();
        } else {
            // 新規ユーザーの場合はINSERT
            final String finalUserId = userId;
            jdbcClient.sql(SQL_INSERT)
                    .param("id", finalUserId)
                    .param("username", user.username())
                    .param("email", user.email())
                    .param("passwordHash", user.passwordHash())
                    .param("enabled", user.enabled())
                    .param("accountNonExpired", user.accountNonExpired())
                    .param("accountNonLocked", user.accountNonLocked())
                    .param("credentialsNonExpired", user.credentialsNonExpired())
                    .param("createdAt", user.createdAt())
                    .param("updatedAt", user.updatedAt())
                    .update();
            
            // IDが生成された場合は、そのIDで返す
            return findById(finalUserId).orElseThrow(() -> 
                    new IllegalStateException("Failed to retrieve user after insertion: " + finalUserId));
        }
        
        // 保存後のユーザーを取得して返す
        return findById(user.id()).orElseThrow(() -> 
                new IllegalStateException("Failed to retrieve user after save: " + user.id()));
    }
    
    public int deleteById(String id) {
        return jdbcClient.sql(SQL_DELETE_BY_ID)
                .param("id", id)
                .update();
    }
    
    @Override
    public int deleteAll() {
        return jdbcClient.sql(SQL_DELETE_ALL)
                .update();
    }
}
