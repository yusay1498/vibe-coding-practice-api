-- テストデータ投入スクリプト
-- このファイルは開発・テスト環境専用です

-- テスト用のデフォルトユーザーを作成
-- パスワード: "admin123" のBCrypt ハッシュ (strength 10)
-- IDはJava側でUUIDを生成して挿入することを想定していますが、
-- テストデータとして固定UUIDを使用します
INSERT INTO users (id, username, email, password_hash, enabled) VALUES
    ('750e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
    ('750e8400-e29b-41d4-a716-446655440002', 'user', 'user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
ON CONFLICT DO NOTHING;

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
