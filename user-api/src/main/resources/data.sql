-- 初期データ投入スクリプト

-- デフォルトロールの作成
-- IDはJava側でUUIDを生成して挿入することを想定していますが、
-- 初期データとして固定UUIDを使用します
INSERT INTO roles (id, name, description) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'ROLE_ADMIN', 'システム管理者 - 全ての権限を持つ'),
    ('550e8400-e29b-41d4-a716-446655440002', 'ROLE_USER', '一般ユーザー - 基本的な権限のみ'),
    ('550e8400-e29b-41d4-a716-446655440003', 'ROLE_MODERATOR', 'モデレーター - 中間レベルの管理権限')
ON CONFLICT (name) DO NOTHING;

-- デフォルト権限の作成
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'USER_READ', 'USER', 'READ', 'ユーザー情報の読み取り'),
    ('650e8400-e29b-41d4-a716-446655440002', 'USER_WRITE', 'USER', 'WRITE', 'ユーザー情報の作成・更新'),
    ('650e8400-e29b-41d4-a716-446655440003', 'USER_DELETE', 'USER', 'DELETE', 'ユーザーの削除'),
    ('650e8400-e29b-41d4-a716-446655440004', 'ROLE_READ', 'ROLE', 'READ', 'ロール情報の読み取り'),
    ('650e8400-e29b-41d4-a716-446655440005', 'ROLE_WRITE', 'ROLE', 'WRITE', 'ロールの作成・更新'),
    ('650e8400-e29b-41d4-a716-446655440006', 'ROLE_DELETE', 'ROLE', 'DELETE', 'ロールの削除'),
    ('650e8400-e29b-41d4-a716-446655440007', 'PERMISSION_READ', 'PERMISSION', 'READ', '権限情報の読み取り'),
    ('650e8400-e29b-41d4-a716-446655440008', 'PERMISSION_WRITE', 'PERMISSION', 'WRITE', '権限の作成・更新'),
    ('650e8400-e29b-41d4-a716-446655440009', 'PERMISSION_DELETE', 'PERMISSION', 'DELETE', '権限の削除'),
    ('650e8400-e29b-41d4-a716-44665544000a', 'AUDIT_READ', 'AUDIT', 'READ', '監査ログの読み取り')
ON CONFLICT (name) DO NOTHING;

-- ロールと権限の関連付け
-- ROLE_ADMIN: 全ての権限
INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles, permissions
WHERE roles.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- ROLE_MODERATOR: ユーザー管理とログ閲覧
INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles, permissions
WHERE roles.name = 'ROLE_MODERATOR'
  AND permissions.name IN ('USER_READ', 'USER_WRITE', 'AUDIT_READ')
ON CONFLICT DO NOTHING;

-- ROLE_USER: 自身の情報の読み取りのみ
INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles, permissions
WHERE roles.name = 'ROLE_USER'
  AND permissions.name IN ('USER_READ')
ON CONFLICT DO NOTHING;
