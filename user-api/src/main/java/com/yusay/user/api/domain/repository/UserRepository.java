package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.model.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ユーザーリポジトリ
 * ユーザーエンティティのデータアクセス層
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * ユーザー名でユーザーを検索
     * @param username ユーザー名
     * @return 該当するユーザー（存在しない場合はEmpty）
     */
    Optional<User> findByUsername(String username);

    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス
     * @return 該当するユーザー（存在しない場合はEmpty）
     */
    Optional<User> findByEmail(String email);

    /**
     * ユーザー名が既に存在するかチェック
     * @param username ユーザー名
     * @return 存在する場合true
     */
    boolean existsByUsername(String username);

    /**
     * メールアドレスが既に存在するかチェック
     * @param email メールアドレス
     * @return 存在する場合true
     */
    boolean existsByEmail(String email);

    /**
     * ユーザーIDでロール情報を含むユーザーを取得
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    @Query("""
        SELECT u.* FROM users u
        WHERE u.id = :userId
        """)
    Optional<User> findUserWithRoles(@Param("userId") Long userId);
}
