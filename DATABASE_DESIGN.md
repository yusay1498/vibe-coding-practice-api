# 認証・認可基盤 - データベース設計書

## 概要

このドキュメントは、Java 17 + Spring Boot 4.0.2で構築されたユーザー認証・認可基盤のデータベース設計を説明します。

## 技術スタック

- **Java**: 17
- **Spring Boot**: 4.0.2
- **データベース**: PostgreSQL
- **ORMフレームワーク**: Spring Data JDBC
- **テストフレームワーク**: JUnit 5 + Testcontainers

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

### デフォルトユーザー

テスト用に2つのユーザーが作成されます：

| ユーザー名 | メール | パスワード | ロール |
|-----------|--------|-----------|--------|
| admin | admin@example.com | admin123 | ROLE_ADMIN |
| user | user@example.com | admin123 | ROLE_USER |

**注意:** 本番環境ではこれらのデフォルトユーザーを削除または変更してください。

### ロールと権限の関連

- **ROLE_ADMIN**: 全ての権限
- **ROLE_MODERATOR**: USER_READ, USER_WRITE, AUDIT_READ
- **ROLE_USER**: USER_READ

## エンティティクラス

### User エンティティ

```java
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Role エンティティ

```java
@Table("roles")
public class Role {
    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Permission エンティティ

```java
@Table("permissions")
public class Permission {
    @Id
    private Long id;
    private String name;
    private String resource;
    private String action;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## Repositoryインターフェース

### UserRepository

主要なメソッド：
- `findByUsername(String username)`: ユーザー名でユーザーを検索
- `findByEmail(String email)`: メールアドレスでユーザーを検索
- `existsByUsername(String username)`: ユーザー名の存在確認
- `existsByEmail(String email)`: メールアドレスの存在確認

### RoleRepository

主要なメソッド：
- `findByName(String name)`: ロール名でロールを検索
- `findByUserId(Long userId)`: ユーザーIDに紐づくロール一覧を取得
- `existsByName(String name)`: ロール名の存在確認

### PermissionRepository

主要なメソッド：
- `findByName(String name)`: 権限名で権限を検索
- `findByResourceAndAction(String resource, String action)`: リソースとアクションで権限を検索
- `findByRoleId(Long roleId)`: ロールIDに紐づく権限一覧を取得
- `findByUserId(Long userId)`: ユーザーIDに紐づく全ての権限を取得

## セットアップ方法

### 1. データベースの起動

Docker Composeを使用してPostgreSQLを起動：

```bash
docker-compose up -d postgres
```

### 2. アプリケーションの起動

```bash
./mvnw spring-boot:run -pl user-api
```

### 3. データベース管理ツール（DbGate）の起動

```bash
docker-compose up -d dbgate
```

DbGateにアクセス: http://localhost:5480

### 4. テストの実行

```bash
./mvnw test -pl user-api
```

## セキュリティ考慮事項

1. **パスワードハッシュ**: BCryptアルゴリズム（strength 10）を使用
2. **外部キー制約**: カスケード削除により、データの整合性を保証
3. **インデックス**: 検索パフォーマンスの最適化
4. **監査ログ**: ユーザーアクションの追跡とセキュリティ監査
5. **アカウント管理フラグ**: 有効/無効、ロック、有効期限の管理

## 今後の拡張

1. **多要素認証（MFA）**: TOTPやSMS認証の追加
2. **OAuth2/OIDC**: ソーシャルログインのサポート
3. **パスワードポリシー**: 複雑さの要件、履歴管理
4. **セッション管理**: 同時ログインの制限
5. **IPホワイトリスト**: アクセス制御の強化

## ライセンス

本プロジェクトのライセンスについては、リポジトリのLICENSEファイルを参照してください。
