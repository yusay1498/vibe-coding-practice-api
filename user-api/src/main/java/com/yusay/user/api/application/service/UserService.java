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

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_ALLOWED_DELETIONS = 1000;
    
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final Clock clock;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public UserService(UserRepository userRepository, UserDomainService userDomainService, Clock clock) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.clock = clock;
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
        
        // 削除前の監査ログ
        List<User> usersToDelete = userRepository.findAll();
        logger.warn("全件削除を開始します。対象ユーザー数: {}, 環境: {}", 
            usersToDelete.size(), activeProfile);
        
        // ドメインルール: 削除件数の検証
        userDomainService.validateDeleteAll(usersToDelete, MAX_ALLOWED_DELETIONS);
        
        // 削除実行
        LocalDateTime executedAt = LocalDateTime.now(clock);
        int deletedCount = userRepository.deleteAll();
        
        // 削除後の監査ログ
        logger.warn("全件削除が完了しました。削除件数: {}, 実行日時: {}, 環境: {}", 
            deletedCount, executedAt, activeProfile);
        
        return new DeleteAllResult(deletedCount, executedAt, activeProfile);
    }
    
    /**
     * 現在の環境が本番環境かどうかを判定
     * 
     * @return 本番環境の場合true
     */
    private boolean isProductionEnvironment() {
        return "prod".equalsIgnoreCase(activeProfile) || 
               "production".equalsIgnoreCase(activeProfile);
    }
}
