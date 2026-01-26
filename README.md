# vibe-coding-practice-api

Practice vibe coding with API as the theme

## プロジェクト概要

このプロジェクトは、Java + Spring Bootを使用したAPIの実装練習です。

## データベース設計

詳細なデータベース設計については、[DATABASE_DESIGN.md](./docs/DATABASE_DESIGN.md)を参照してください。

## セットアップ

### 前提条件

- Docker & Docker Compose

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

本プロジェクトのライセンスについては、LICENSEファイルを参照してください。

