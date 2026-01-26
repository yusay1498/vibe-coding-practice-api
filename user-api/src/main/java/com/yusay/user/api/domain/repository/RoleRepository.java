package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.model.Role;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ロールリポジトリ
 * ロールエンティティのデータアクセス層
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

    /**
     * ロール名でロールを検索
     * @param name ロール名
     * @return 該当するロール（存在しない場合はEmpty）
     */
    Optional<Role> findByName(String name);

    /**
     * ユーザーIDに紐づくロール一覧を取得
     * @param userId ユーザーID
     * @return ロールのリスト
     */
    @Query("""
        SELECT r.* FROM roles r
        INNER JOIN user_roles ur ON r.id = ur.role_id
        WHERE ur.user_id = :userId
        """)
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * ロール名が既に存在するかチェック
     * @param name ロール名
     * @return 存在する場合true
     */
    boolean existsByName(String name);
}
