-- テストデータ: 無効化されたユーザー
INSERT INTO users (id, username, email, password_hash, enabled,
                   account_non_expired, account_non_locked, credentials_non_expired,
                   created_at, updated_at)
VALUES ('disabled-user-id', 'disableduser', 'disabled@example.com', '$2a$10$disabled-hash',
        false, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
