-- 初期データ投入スクリプト

-- デフォルトロールの作成
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'システム管理者 - 全ての権限を持つ'),
    ('ROLE_USER', '一般ユーザー - 基本的な権限のみ'),
    ('ROLE_MODERATOR', 'モデレーター - 中間レベルの管理権限')
ON CONFLICT (name) DO NOTHING;

-- デフォルト権限の作成
INSERT INTO permissions (name, resource, action, description) VALUES
    ('USER_READ', 'USER', 'READ', 'ユーザー情報の読み取り'),
    ('USER_WRITE', 'USER', 'WRITE', 'ユーザー情報の作成・更新'),
    ('USER_DELETE', 'USER', 'DELETE', 'ユーザーの削除'),
    ('ROLE_READ', 'ROLE', 'READ', 'ロール情報の読み取り'),
    ('ROLE_WRITE', 'ROLE', 'WRITE', 'ロールの作成・更新'),
    ('ROLE_DELETE', 'ROLE', 'DELETE', 'ロールの削除'),
    ('PERMISSION_READ', 'PERMISSION', 'READ', '権限情報の読み取り'),
    ('PERMISSION_WRITE', 'PERMISSION', 'WRITE', '権限の作成・更新'),
    ('PERMISSION_DELETE', 'PERMISSION', 'DELETE', '権限の削除'),
    ('AUDIT_READ', 'AUDIT', 'READ', '監査ログの読み取り')
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
