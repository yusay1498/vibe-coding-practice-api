# 認証・認可基盤 - データベース設計書

## 概要

このドキュメントは、ユーザー認証・認可基盤のデータベース設計を説明します。

## 使用データベース

- **データベース**: PostgreSQL

## データベース設計

### ER図の概念

```
users (ユーザー) ←→ user_roles ←→ roles (ロール)
                                      ↕
                               role_permissions
                                      ↕
                               permissions (権限)
```

### テーブル一覧

#### 1. `users` - ユーザーテーブル

ユーザーの基本情報を格納するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGSERIAL | PRIMARY KEY | ユーザーID（自動採番） |
| username | VARCHAR(50) | NOT NULL, UNIQUE | ユーザー名 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | メールアドレス |
| password_hash | VARCHAR(255) | NOT NULL | パスワードハッシュ（BCrypt） |
| enabled | BOOLEAN | NOT NULL, DEFAULT true | アカウント有効フラグ |
| account_non_expired | BOOLEAN | NOT NULL, DEFAULT true | アカウント有効期限切れフラグ |
| account_non_locked | BOOLEAN | NOT NULL, DEFAULT true | アカウントロックフラグ |
| credentials_non_expired | BOOLEAN | NOT NULL, DEFAULT true | 認証情報有効期限切れフラグ |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 更新日時 |

**インデックス:**
- `idx_users_username` on `username`
- `idx_users_email` on `email`

#### 2. `roles` - ロールテーブル

システムで使用する役割（ロール）を定義するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGSERIAL | PRIMARY KEY | ロールID（自動採番） |
| name | VARCHAR(50) | NOT NULL, UNIQUE | ロール名（例: ROLE_ADMIN） |
| description | VARCHAR(255) | | ロールの説明 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 更新日時 |

**デフォルトロール:**
- `ROLE_ADMIN`: システム管理者（全権限）
- `ROLE_USER`: 一般ユーザー（基本権限のみ）
- `ROLE_MODERATOR`: モデレーター（中間レベルの管理権限）

#### 3. `user_roles` - ユーザー・ロール関連テーブル

ユーザーとロールの多対多の関係を管理するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| user_id | BIGINT | NOT NULL, FK → users.id | ユーザーID |
| role_id | BIGINT | NOT NULL, FK → roles.id | ロールID |
| assigned_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 割り当て日時 |

**主キー:** (user_id, role_id)

**外部キー制約:**
- `ON DELETE CASCADE`: ユーザーまたはロールが削除されると、関連レコードも削除

#### 4. `permissions` - 権限テーブル

システムで使用する権限（パーミッション）を定義するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGSERIAL | PRIMARY KEY | 権限ID（自動採番） |
| name | VARCHAR(100) | NOT NULL, UNIQUE | 権限名（例: USER_READ） |
| resource | VARCHAR(100) | NOT NULL | リソース名（例: USER） |
| action | VARCHAR(50) | NOT NULL | アクション名（READ, WRITE, DELETE） |
| description | VARCHAR(255) | | 権限の説明 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 更新日時 |

**インデックス:**
- `idx_permissions_resource` on `resource`
- `idx_permissions_action` on `action`

**デフォルト権限:**
- ユーザー管理: `USER_READ`, `USER_WRITE`, `USER_DELETE`
- ロール管理: `ROLE_READ`, `ROLE_WRITE`, `ROLE_DELETE`
- 権限管理: `PERMISSION_READ`, `PERMISSION_WRITE`, `PERMISSION_DELETE`
- 監査ログ: `AUDIT_READ`

#### 5. `role_permissions` - ロール・権限関連テーブル

ロールと権限の多対多の関係を管理するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| role_id | BIGINT | NOT NULL, FK → roles.id | ロールID |
| permission_id | BIGINT | NOT NULL, FK → permissions.id | 権限ID |
| assigned_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 割り当て日時 |

**主キー:** (role_id, permission_id)

**外部キー制約:**
- `ON DELETE CASCADE`: ロールまたは権限が削除されると、関連レコードも削除

#### 6. `refresh_tokens` - リフレッシュトークンテーブル（オプション）

JWT認証を使用する場合のリフレッシュトークンを管理するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGSERIAL | PRIMARY KEY | トークンID（自動採番） |
| user_id | BIGINT | NOT NULL, FK → users.id | ユーザーID |
| token | VARCHAR(255) | NOT NULL, UNIQUE | リフレッシュトークン |
| expiry_date | TIMESTAMP | NOT NULL | 有効期限 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 作成日時 |

**インデックス:**
- `idx_refresh_tokens_user_id` on `user_id`
- `idx_refresh_tokens_token` on `token`

**外部キー制約:**
- `ON DELETE CASCADE`: ユーザーが削除されると、関連トークンも削除

#### 7. `audit_logs` - 監査ログテーブル（オプション）

セキュリティとコンプライアンスのための監査証跡を記録するテーブル。

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGSERIAL | PRIMARY KEY | ログID（自動採番） |
| user_id | BIGINT | FK → users.id | ユーザーID |
| action | VARCHAR(100) | NOT NULL | アクション（例: LOGIN, LOGOUT） |
| resource | VARCHAR(100) | | 対象リソース |
| details | TEXT | | 詳細情報 |
| ip_address | VARCHAR(45) | | IPアドレス（IPv6対応） |
| user_agent | VARCHAR(255) | | ユーザーエージェント |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 作成日時 |

**インデックス:**
- `idx_audit_logs_user_id` on `user_id`
- `idx_audit_logs_action` on `action`
- `idx_audit_logs_created_at` on `created_at`

**外部キー制約:**
- `ON DELETE SET NULL`: ユーザーが削除されても、監査ログは保持（user_idがNULLになる）

## 初期データ

### 本番環境用データ（data.sql）

- **デフォルトロール**: ROLE_ADMIN, ROLE_USER, ROLE_MODERATOR
- **デフォルト権限**: ユーザー管理、ロール管理、権限管理、監査ログの各種権限
- **ロールと権限の関連付け**: 各ロールに適切な権限を割り当て

### テスト環境用データ（test-data.sql）

テスト用に2つのユーザーが `user-api/src/test/resources/test-data.sql` に定義されています：

| ユーザー名 | メール | パスワード | ロール |
|-----------|--------|-----------|--------|
| admin | admin@example.com | admin123 | ROLE_ADMIN |
| user | user@example.com | admin123 | ROLE_USER |

**注意**: これらのユーザーはテスト専用です。本番環境では `data.sql` のみを使用してください。

### ロールと権限の関連

- **ROLE_ADMIN**: 全ての権限
- **ROLE_MODERATOR**: USER_READ, USER_WRITE, AUDIT_READ
- **ROLE_USER**: USER_READ

## セットアップ方法

### 1. データベースの起動

Docker Composeを使用してPostgreSQLを起動：

```bash
docker-compose up -d postgres
```

データベースは自動的に `schema.sql` と `data.sql` を実行して初期化されます。

### 2. データベース管理ツール（DbGate）の起動

```bash
docker-compose up -d dbgate
```

DbGateにアクセス: http://localhost:5480

## セキュリティ考慮事項

1. **パスワードハッシュ**: BCryptアルゴリズム（strength 10）を使用
2. **外部キー制約**: カスケード削除により、データの整合性を保証
3. **インデックス**: 検索パフォーマンスの最適化
4. **監査ログ**: ユーザーアクションの追跡とセキュリティ監査
5. **アカウント管理フラグ**: 有効/無効、ロック、有効期限の管理

## 今後の拡張

1. **多要素認証（MFA）**: TOTPやSMS認証のテーブル追加
2. **OAuth2/OIDC**: ソーシャルログイン用のテーブル設計
3. **パスワードポリシー**: パスワード履歴管理テーブル
4. **セッション管理**: セッション情報を格納するテーブル
5. **IPホワイトリスト**: アクセス制御用のテーブル

## ライセンス

本プロジェクトのライセンスについては、リポジトリのLICENSEファイルを参照してください。
