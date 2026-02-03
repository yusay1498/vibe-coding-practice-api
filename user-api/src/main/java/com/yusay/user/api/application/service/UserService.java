package com.yusay.user.api.application.service;

import com.yusay.user.api.application.dto.DeleteAllResult;
import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import com.yusay.user.api.domain.exception.DuplicateUserException;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import com.yusay.user.api.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final String activeProfile;
    private final int maxAllowedDeletions;

    public UserService(
            UserRepository userRepository, 
            UserDomainService userDomainService,
            @Value("${spring.profiles.active:default}") String activeProfile,
            @Value("${user.delete-all.max-allowed-deletions:1000}") int maxAllowedDeletions) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.activeProfile = activeProfile;
        
        // maxAllowedDeletionsの妥当性検証
        if (maxAllowedDeletions <= 0) {
            throw new IllegalArgumentException(
                String.format("maxAllowedDeletions must be positive, but was: %d", maxAllowedDeletions));
        }
        this.maxAllowedDeletions = maxAllowedDeletions;
    }

    /**
     * 新規ユーザーを作成する
     * 
     * @param username ユーザー名
     * @param email メールアドレス
     * @param passwordHash パスワードハッシュ
     * @return 作成されたユーザー
     * @throws DuplicateUserException メールアドレスまたはユーザー名が既に存在する場合
     */
    public User create(String username, String email, String passwordHash) {
        // メールアドレスの重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException(email);
        }
        
        // ユーザー名の重複チェック
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUserException(username);
        }
        
        // ドメインサービスを使用してユーザーを作成
        User newUser = userDomainService.createUser(
                null,  // IDはリポジトリ層で生成される
                username,
                email,
                passwordHash,
                true,  // enabled
                true,  // accountNonExpired
                true,  // accountNonLocked
                true   // credentialsNonExpired
        );
        
        // リポジトリに保存
        // 競合状態（race condition）に対処するため、UNIQUE制約違反を捕捉
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            // 同時リクエストにより重複チェック後にデータが挿入された場合
            // データベースのUNIQUE制約により例外が発生するため、適切な例外に変換
            if (userRepository.findByEmail(email).isPresent()) {
                throw new DuplicateUserException(email);
            }
            if (userRepository.findByUsername(username).isPresent()) {
                throw new DuplicateUserException(username);
            }
            // 他のデータ整合性エラーの場合は元の例外を再スロー
            throw e;
        }
    }

    public User lookup(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    /**
     * 既存ユーザーの情報を更新する
     * 
     * @param id ユーザーID
     * @param username 新しいユーザー名（nullの場合は既存値を保持）
     * @param email 新しいメールアドレス（nullの場合は既存値を保持）
     * @param passwordHash 新しいパスワードハッシュ（nullの場合は既存値を保持）
     * @param enabled 新しい有効フラグ（nullの場合は既存値を保持）
     * @param accountNonExpired 新しいアカウント有効期限切れフラグ（nullの場合は既存値を保持）
     * @param accountNonLocked 新しいアカウントロックフラグ（nullの場合は既存値を保持）
     * @param credentialsNonExpired 新しい認証情報有効期限切れフラグ（nullの場合は既存値を保持）
     * @return 更新されたユーザー
     * @throws UserNotFoundException ユーザーが見つからない場合
     * @throws DuplicateUserException メールアドレスまたはユーザー名が他のユーザーと重複する場合
     */
    public User update(
            String id,
            String username,
            String email,
            String passwordHash,
            Boolean enabled,
            Boolean accountNonExpired,
            Boolean accountNonLocked,
            Boolean credentialsNonExpired) {
        
        // 更新対象のユーザーを取得
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // メールアドレスの重複チェック（変更される場合のみ）
        if (email != null && !email.equals(existingUser.email())) {
            Optional<User> userWithEmail = userRepository.findByEmail(email);
            if (userWithEmail.isPresent() && !userWithEmail.get().id().equals(id)) {
                // 自分自身以外のユーザーがそのメールアドレスを使用している場合は例外
                throw new DuplicateUserException(email);
            }
        }
        
        // ユーザー名の重複チェック（変更される場合のみ）
        if (username != null && !username.equals(existingUser.username())) {
            Optional<User> userWithUsername = userRepository.findByUsername(username);
            if (userWithUsername.isPresent() && !userWithUsername.get().id().equals(id)) {
                // 自分自身以外のユーザーがそのユーザー名を使用している場合は例外
                throw new DuplicateUserException(username);
            }
        }
        
        // ドメインサービスを使用してユーザーを更新
        User updatedUser = userDomainService.updateUser(
                existingUser,
                username,
                email,
                passwordHash,
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired
        );
        
        // リポジトリに保存
        // 競合状態（race condition）に対処するため、UNIQUE制約違反を捕捉
        try {
            return userRepository.save(updatedUser);
        } catch (DataIntegrityViolationException e) {
            // 同時リクエストにより重複チェック後にデータが変更された場合
            // データベースのUNIQUE制約により例外が発生するため、適切な例外に変換
            if (email != null) {
                Optional<User> userWithEmail = userRepository.findByEmail(email);
                if (userWithEmail.isPresent() && !userWithEmail.get().id().equals(id)) {
                    throw new DuplicateUserException(email);
                }
            }
            if (username != null) {
                Optional<User> userWithUsername = userRepository.findByUsername(username);
                if (userWithUsername.isPresent() && !userWithUsername.get().id().equals(id)) {
                    throw new DuplicateUserException(username);
                }
            }
            // 他のデータ整合性エラーの場合は元の例外を再スロー
            throw e;
        }
    }

    public void delete(String id) {
        int deletedCount = userRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new UserNotFoundException(id);
        }
    }

    /**
     * 全ユーザーを削除する（環境制限と検証付き）
     * 
     * アプリケーションロジック:
     * - 本番環境では実行を拒否
     * - 削除前後の監査ログを記録
     * 
     * ドメインルール:
     * - 削除対象が上限を超える場合は拒否
     * 
     * @return 削除結果（削除件数、実行日時、環境）
     * @throws DeleteAllNotAllowedException 本番環境での実行または削除件数が上限を超える場合
     */
    public DeleteAllResult deleteAll() {
        // アプリケーションロジック: 本番環境チェック
        if (isProductionEnvironment()) {
            logger.error("本番環境での全件削除が試行されました。環境: {}", activeProfile);
            throw new DeleteAllNotAllowedException("本番環境では全件削除を実行できません。");
        }
        
        // 削除前の検証（監査ログは検証成功後に記録）
        List<User> usersToDelete = userRepository.findAll();
        
        // ドメインルール: 削除件数の事前検証
        try {
            userDomainService.validateDeleteAll(usersToDelete, maxAllowedDeletions);
        } catch (DeleteAllNotAllowedException e) {
            logger.error("全件削除の事前検証で失敗しました。対象ユーザー数: {}, 上限: {}, 環境: {}",
                usersToDelete.size(), maxAllowedDeletions, activeProfile);
            throw e;
        }
        
        // 検証成功後の監査ログ
        logger.warn("全件削除を開始します。対象ユーザー数: {}, 環境: {}", 
            usersToDelete.size(), activeProfile);
        
        // 削除実行
        LocalDateTime executedAt = userDomainService.getCurrentTime();
        int deletedCount = userRepository.deleteAll();
        
        // ドメインルール: 削除後の実際の削除件数を再検証し、競合状態を検出
        // 注意: この検証は、事前検証と削除実行の間にデータが追加された場合（競合状態）や、
        // deleteAll()の実装がfindAll()と異なる挙動をする場合（実装バグ）を検出するためのものです。
        // 通常の競合状態では、事前検証で上限以下と判定されたデータが削除されるため、
        // この検証で上限を超えることはほぼありませんが、安全性のために実装しています。
        if (deletedCount > maxAllowedDeletions) {
            logger.error("全件削除で削除件数が上限を超えました。削除件数: {}, 上限: {}, 環境: {}",
                deletedCount, maxAllowedDeletions, activeProfile);
            // @Transactional により、この例外で deleteAll の削除はロールバックされる
            throw new DeleteAllNotAllowedException(
                String.format("削除件数（%d件）が上限（%d件）を超えているため、全件削除をロールバックしました。",
                    deletedCount, maxAllowedDeletions));
        }
        
        // 削除後の監査ログ
        logger.warn("全件削除が完了しました。削除件数: {}, 実行日時: {}, 環境: {}", 
            deletedCount, executedAt, activeProfile);
        
        return new DeleteAllResult(deletedCount, executedAt, activeProfile);
    }
    
    /**
     * 現在の環境が本番環境かどうかを判定
     * 複数プロファイルのカンマ区切りにも対応
     * 
     * @return 本番環境の場合true
     */
    private boolean isProductionEnvironment() {
        if (activeProfile == null || activeProfile.isBlank()) {
            return false;
        }

        String[] profiles = activeProfile.split(",");
        for (String profile : profiles) {
            String trimmed = profile.trim();
            if ("prod".equalsIgnoreCase(trimmed) || "production".equalsIgnoreCase(trimmed)) {
                return true;
            }
        }

        return false;
    }
}
