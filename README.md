# vibe-coding-practice-api

Practice vibe coding with API as the theme

## プロジェクト概要

このプロジェクトは、Java 17とSpring Boot 4.0.2を使用したユーザー認証・認可APIの実装です。

## 技術スタック

- **Java**: 17
- **Spring Boot**: 4.0.2
- **データベース**: PostgreSQL
- **ORMフレームワーク**: Spring Data JDBC
- **認証・認可**: Spring Security
- **テストフレームワーク**: JUnit 5 + Testcontainers
- **ビルドツール**: Maven

## 主要機能

### 認証・認可基盤

- ✅ ユーザー管理（登録、更新、削除）
- ✅ ロールベースアクセス制御（RBAC）
- ✅ 権限管理
- ✅ 監査ログ機能
- ✅ リフレッシュトークン管理（JWT用）

## データベース設計

詳細なデータベース設計については、[DATABASE_DESIGN.md](./DATABASE_DESIGN.md)を参照してください。

### 主要テーブル

- `users` - ユーザー基本情報
- `roles` - ロール定義
- `permissions` - 権限定義
- `user_roles` - ユーザーとロールの関連
- `role_permissions` - ロールと権限の関連
- `refresh_tokens` - リフレッシュトークン（JWT用）
- `audit_logs` - 監査ログ

## セットアップ

### 前提条件

- Java 17以上
- Docker & Docker Compose
- Maven 3.6以上

### 1. リポジトリのクローン

```bash
git clone https://github.com/yusay1498/vibe-coding-practice-api.git
cd vibe-coding-practice-api
```

### 2. データベースの起動

```bash
docker-compose up -d postgres
```

### 3. アプリケーションのビルド

```bash
./mvnw clean package -pl user-api
```

### 4. アプリケーションの起動

```bash
./mvnw spring-boot:run -pl user-api
```

または、ビルドしたJARファイルを実行：

```bash
java -jar user-api/target/user-api-0.0.1-SNAPSHOT.jar
```

### 5. データベース管理ツール（DbGate）の起動（オプション）

```bash
docker-compose up -d dbgate
```

DbGateにアクセス: http://localhost:5480

## テスト

### 全テストの実行

```bash
./mvnw test -pl user-api
```

### テストカバレッジ

現在、以下のテストが実装されています：
- ユーザーリポジトリテスト（8テスト）
- ロールリポジトリテスト（7テスト）
- 権限リポジトリテスト（7テスト）

## デフォルトユーザー

開発・テスト用に以下のユーザーが事前に作成されています：

| ユーザー名 | メール | パスワード | ロール |
|-----------|--------|-----------|--------|
| admin | admin@example.com | admin123 | ROLE_ADMIN |
| user | user@example.com | admin123 | ROLE_USER |

**⚠️ 警告**: 本番環境では必ずこれらのデフォルトユーザーを削除または変更してください。

## プロジェクト構造

```
vibe-coding-practice-api/
├── user-api/                    # ユーザーAPIモジュール
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/yusay/user/api/
│   │   │   │       ├── application/      # アプリケーション層
│   │   │   │       ├── domain/           # ドメイン層
│   │   │   │       │   ├── model/        # エンティティ
│   │   │   │       │   └── repository/   # リポジトリ
│   │   │   │       ├── infrastructure/   # インフラストラクチャ層
│   │   │   │       └── presentation/     # プレゼンテーション層
│   │   │   └── resources/
│   │   │       ├── schema.sql           # DDLスクリプト
│   │   │       ├── data.sql             # 初期データ
│   │   │       └── application.yaml     # 設定ファイル
│   │   └── test/                        # テストコード
│   └── pom.xml
├── compose.yml                          # Docker Compose設定
├── DATABASE_DESIGN.md                   # DB設計書
└── README.md
```

## 開発ガイド

### レイヤードアーキテクチャ

プロジェクトは以下のレイヤーで構成されています：

1. **プレゼンテーション層** (`presentation`): REST API エンドポイント
2. **アプリケーション層** (`application`): ビジネスロジックの調整
3. **ドメイン層** (`domain`): エンティティとリポジトリ
4. **インフラストラクチャ層** (`infrastructure`): 外部サービスとの連携

### コーディング規約

- Java 17の機能を活用
- テストファーストの開発
- 日本語でのコメント・ドキュメント記述

## ライセンス

本プロジェクトのライセンスについては、LICENSEファイルを参照してください。

## コントリビューション

プルリクエストを歓迎します。大きな変更の場合は、まずIssueを開いて変更内容を議論してください。

