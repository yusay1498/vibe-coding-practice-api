package com.yusay.user.api.domain.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ユーザーエンティティの生成・更新ロジックを担当するドメインサービス
 */
@Service
public class UserDomainService {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final Clock clock;

    public UserDomainService(Clock clock) {
        this.clock = clock;
    }

    /**
     * 新規ユーザーを作成する際に作成日時を自動的に設定する
     * ドメインルール: 入力値の妥当性を検証する
     * 
     * @param id ユーザーID（nullの場合はリポジトリ層で生成される）
     * @param username ユーザー名
     * @param email メールアドレス
     * @param passwordHash パスワードハッシュ
     * @param enabled 有効フラグ
     * @param accountNonExpired アカウント有効期限切れフラグ
     * @param accountNonLocked アカウントロックフラグ
     * @param credentialsNonExpired 認証情報有効期限切れフラグ
     * @return 作成日時と更新日時が設定された新規Userインスタンス
     * @throws IllegalArgumentException 入力値が不正な場合
     */
    public User createUser(
            String id,
            String username,
            String email,
            String passwordHash,
            Boolean enabled,
            Boolean accountNonExpired,
            Boolean accountNonLocked,
            Boolean credentialsNonExpired
    ) {
        // ドメインルール: 入力値のバリデーション
        validateUsername(username);
        validateEmail(email);
        validatePasswordHash(passwordHash);
        validateBooleanFields(enabled, accountNonExpired, accountNonLocked, credentialsNonExpired);
        
        LocalDateTime now = LocalDateTime.now(clock);
        return new User(
                id,
                username,
                email,
                passwordHash,
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired,
                now,  // createdAt
                now   // updatedAt
        );
    }

    /**
     * 既存ユーザーを更新する際に更新日時を自動的に設定する
     * 
     * @param existingUser 既存のユーザー
     * @param username 新しいユーザー名（nullの場合は既存値を保持）
     * @param email 新しいメールアドレス（nullの場合は既存値を保持）
     * @param passwordHash 新しいパスワードハッシュ（nullの場合は既存値を保持）
     * @param enabled 新しい有効フラグ（nullの場合は既存値を保持）
     * @param accountNonExpired 新しいアカウント有効期限切れフラグ（nullの場合は既存値を保持）
     * @param accountNonLocked 新しいアカウントロックフラグ（nullの場合は既存値を保持）
     * @param credentialsNonExpired 新しい認証情報有効期限切れフラグ（nullの場合は既存値を保持）
     * @return 更新日時が設定された更新後のUserインスタンス
     */
    public User updateUser(
            User existingUser,
            String username,
            String email,
            String passwordHash,
            Boolean enabled,
            Boolean accountNonExpired,
            Boolean accountNonLocked,
            Boolean credentialsNonExpired
    ) {
        return new User(
                existingUser.id(),
                username != null ? username : existingUser.username(),
                email != null ? email : existingUser.email(),
                passwordHash != null ? passwordHash : existingUser.passwordHash(),
                enabled != null ? enabled : existingUser.enabled(),
                accountNonExpired != null ? accountNonExpired : existingUser.accountNonExpired(),
                accountNonLocked != null ? accountNonLocked : existingUser.accountNonLocked(),
                credentialsNonExpired != null ? credentialsNonExpired : existingUser.credentialsNonExpired(),
                existingUser.createdAt(),  // 作成日時は保持
                LocalDateTime.now(clock)  // 更新日時を現在時刻に設定
        );
    }

    /**
     * 全件削除の実行前検証を行う
     * ドメインルール: 削除対象が一定数を超える場合は安全性のために拒否する
     * 
     * 注意: 空のリスト（0件）の削除は許可されます。これはデータが存在しない状態での
     * 全件削除操作を安全に実行できるようにするためです。
     * 
     * @param users 削除対象のユーザーリスト
     * @param maxAllowedDeletions 一度に削除可能な最大件数（正の整数である必要があります）
     * @throws DeleteAllNotAllowedException 削除が許可されていない場合
     * @throws IllegalArgumentException maxAllowedDeletionsが0以下の場合
     */
    public void validateDeleteAll(List<User> users, int maxAllowedDeletions) {
        if (maxAllowedDeletions <= 0) {
            throw new IllegalArgumentException(
                String.format("maxAllowedDeletions must be positive, but was: %d", maxAllowedDeletions));
        }
        
        int userCount = users.size();
        
        if (userCount > maxAllowedDeletions) {
            throw new DeleteAllNotAllowedException(
                String.format("削除対象ユーザー数（%d件）が上限（%d件）を超えています。安全のため削除を拒否します。",
                    userCount, maxAllowedDeletions)
            );
        }
    }

    /**
     * 現在時刻を取得する
     * 
     * @return 現在のLocalDateTime
     */
    public LocalDateTime getCurrentTime() {
        return LocalDateTime.now(clock);
    }
    
    /**
     * ユーザー名のバリデーション
     * ドメインルール: ユーザー名は3文字以上50文字以内
     * 
     * @param username ユーザー名
     * @throws IllegalArgumentException ユーザー名が不正な場合
     */
    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("ユーザー名は3文字以上で入力してください");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("ユーザー名は50文字以内で入力してください");
        }
    }
    
    /**
     * メールアドレスのバリデーション
     * ドメインルール: 有効な形式のメールアドレス、100文字以内
     * 
     * @param email メールアドレス
     * @throws IllegalArgumentException メールアドレスが不正な場合
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("メールアドレスは100文字以内で入力してください");
        }
        // 基本的なメールアドレス形式チェック
        if (!email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("有効なメールアドレスを入力してください");
        }
    }
    
    /**
     * パスワードハッシュのバリデーション
     * ドメインルール: パスワードハッシュは必須
     * 
     * @param passwordHash パスワードハッシュ
     * @throws IllegalArgumentException パスワードハッシュが不正な場合
     */
    private void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("パスワードハッシュは必須です");
        }
    }
    
    /**
     * Boolean型フィールドのバリデーション
     * ドメインルール: すべてのBoolean型フィールドは必須
     * 
     * @param enabled 有効フラグ
     * @param accountNonExpired アカウント有効期限切れフラグ
     * @param accountNonLocked アカウントロックフラグ
     * @param credentialsNonExpired 認証情報有効期限切れフラグ
     * @throws IllegalArgumentException いずれかのフィールドがnullの場合
     */
    private void validateBooleanFields(Boolean enabled, Boolean accountNonExpired, 
                                      Boolean accountNonLocked, Boolean credentialsNonExpired) {
        if (enabled == null) {
            throw new IllegalArgumentException("有効フラグは必須です");
        }
        if (accountNonExpired == null) {
            throw new IllegalArgumentException("アカウント有効期限切れフラグは必須です");
        }
        if (accountNonLocked == null) {
            throw new IllegalArgumentException("アカウントロックフラグは必須です");
        }
        if (credentialsNonExpired == null) {
            throw new IllegalArgumentException("認証情報有効期限切れフラグは必須です");
        }
    }
}
