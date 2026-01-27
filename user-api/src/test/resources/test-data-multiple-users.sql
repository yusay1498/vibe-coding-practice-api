-- テストデータ: 複数ユーザー
INSERT INTO users (id, username, email, password_hash, enabled,
                   account_non_expired, account_non_locked, credentials_non_expired,
                   created_at, updated_at)
VALUES ('test-user-id-001', 'user1', 'user1@example.com', '$2a$10$hash1',
        true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password_hash, enabled,
                   account_non_expired, account_non_locked, credentials_non_expired,
                   created_at, updated_at)
VALUES ('test-user-id-002', 'user2', 'user2@example.com', '$2a$10$hash2',
        true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
