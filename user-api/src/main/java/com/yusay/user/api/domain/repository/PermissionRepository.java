package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.model.Permission;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 権限リポジトリ
 * 権限エンティティのデータアクセス層
 */
@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long> {

    /**
     * 権限名で権限を検索
     * @param name 権限名
     * @return 該当する権限（存在しない場合はEmpty）
     */
    Optional<Permission> findByName(String name);

    /**
     * リソースとアクションで権限を検索
     * @param resource リソース名
     * @param action アクション名
     * @return 該当する権限のリスト
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * ロールIDに紐づく権限一覧を取得
     * @param roleId ロールID
     * @return 権限のリスト
     */
    @Query("""
        SELECT p.* FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        WHERE rp.role_id = :roleId
        """)
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * ユーザーIDに紐づく全ての権限を取得（ユーザーの全ロールから）
     * @param userId ユーザーID
     * @return 権限のリスト
     */
    @Query("""
        SELECT DISTINCT p.* FROM permissions p
        INNER JOIN role_permissions rp ON p.id = rp.permission_id
        INNER JOIN user_roles ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = :userId
        """)
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 権限名が既に存在するかチェック
     * @param name 権限名
     * @return 存在する場合true
     */
    boolean existsByName(String name);
}
