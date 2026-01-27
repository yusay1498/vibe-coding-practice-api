# vibe-coding-practice-api

Practice vibe coding with API as the theme

## プロジェクト概要

このプロジェクトは、Java + Spring Bootを使用したAPIの実装練習です。

## データベース設計

詳細なデータベース設計については、[DATABASE_DESIGN.md](./docs/DATABASE_DESIGN.md)を参照してください。

## セットアップ

### 前提条件

- Docker & Docker Compose
- Java 25 (またはDevcontainerを使用)

### 開発環境のセットアップ

#### オプション1: Devcontainer（推奨）

このプロジェクトにはJava 25環境を自動セットアップする`.devcontainer`設定が含まれています。

VS CodeまたはGitHub Codespaces/Copilot Workspaceで：
1. プロジェクトを開く
2. "Reopen in Container"を選択
3. 自動的にJava 25環境がセットアップされます

#### オプション2: ローカル環境

Java 25を手動でインストールして使用することもできます。

### データベースの起動

```bash
docker-compose up -d postgres
```

### データベース管理ツール（DbGate）の起動

```bash
docker-compose up -d dbgate
```

DbGateにアクセス: http://localhost:5480

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。詳細は[LICENSE](./LICENSE)ファイルを参照してください。

