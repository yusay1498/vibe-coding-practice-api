-- テストデータ: 単一ユーザー
INSERT INTO users (id, username, email, password_hash, enabled,
                   account_non_expired, account_non_locked, credentials_non_expired,
                   created_at, updated_at)
VALUES ('test-user-id-001', 'testuser', 'test@example.com', '$2a$10$test-password-hash',
        true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
