# findById ユーザーAPI エンドポイント追加

より広範な実装を行う前に、アーキテクチャアプローチを検証するために、単一の `GET /api/users/{id}` エンドポイントを持つユーザー認証基盤の初期実装です。

## 変更内容

### コア実装
- `User` エンティティ - 既存の `users` テーブルスキーマへのマッピング
- `UserRepository` - Spring Data JDBCの`CrudRepository`を継承
- `UserService` - `findById(String id)` メソッドを提供
- `UserController` - `GET /api/users/{id}` を公開し、200/404を返却

### 設定
- `spring.sql.init.mode=always` によるデータベーススキーマの自動初期化
- 開発用に認証とCSRF保護を無効化したセキュリティ設定（本番環境用の警告付き）
- Java 17 をターゲット（環境制約のため）

### テスト
- Testcontainers PostgreSQLを使用した統合テスト
- 存在しないユーザーに対する404レスポンスの検証

## API動作

```bash
# 存在しないユーザー
GET /api/users/invalid-id
→ 404 Not Found

# 存在するユーザー
GET /api/users/550e8400-e29b-41d4-a716-446655440001
→ 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "username": "admin",
  "email": "admin@example.com",
  "enabled": true,
  ...
}
```

## セキュリティに関する注意

CSRF保護と認証は現在開発用に無効化されています。本番環境へのデプロイ時には、セキュリティコントロールを有効化する必要があります。
