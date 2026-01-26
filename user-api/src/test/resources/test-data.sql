-- テストデータ投入スクリプト
-- このファイルは開発・テスト環境専用です

-- テスト用のデフォルトユーザーを作成
-- パスワード: "admin123" のBCrypt ハッシュ (strength 10)
INSERT INTO users (username, email, password_hash, enabled) VALUES
    ('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
    ('user', 'user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
ON CONFLICT (username) DO NOTHING;

-- テストユーザーにロールを割り当て
INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users, roles
WHERE users.username = 'admin' AND roles.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users, roles
WHERE users.username = 'user' AND roles.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;
