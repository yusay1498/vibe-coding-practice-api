package com.yusay.user.api.domain.repository;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.domain.model.Permission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PermissionRepositoryのテスト
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@DisplayName("権限リポジトリのテスト")
class PermissionRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("権限名で権限を検索できる")
    void findByName_ShouldReturnPermission_WhenPermissionExists() {
        // When
        Optional<Permission> permission = permissionRepository.findByName("USER_READ");

        // Then
        assertThat(permission).isPresent();
        assertThat(permission.get().getName()).isEqualTo("USER_READ");
        assertThat(permission.get().getResource()).isEqualTo("USER");
        assertThat(permission.get().getAction()).isEqualTo("READ");
    }

    @Test
    @DisplayName("リソースとアクションで権限を検索できる")
    void findByResourceAndAction_ShouldReturnPermissions() {
        // When
        List<Permission> permissions = permissionRepository.findByResourceAndAction("USER", "READ");

        // Then
        assertThat(permissions).isNotEmpty();
        assertThat(permissions).allMatch(p -> 
            p.getResource().equals("USER") && p.getAction().equals("READ")
        );
    }

    @Test
    @DisplayName("ロールIDに紐づく権限一覧を取得できる")
    void findByRoleId_ShouldReturnPermissions_WhenRoleHasPermissions() {
        // Given
        var adminRole = roleRepository.findByName("ROLE_ADMIN");
        assertThat(adminRole).isPresent();

        // When
        List<Permission> permissions = permissionRepository.findByRoleId(adminRole.get().getId());

        // Then
        assertThat(permissions).isNotEmpty();
        assertThat(permissions.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("ユーザーIDに紐づく全ての権限を取得できる")
    void findByUserId_ShouldReturnPermissions_WhenUserHasPermissions() {
        // Given
        var adminUser = userRepository.findByUsername("admin");
        assertThat(adminUser).isPresent();

        // When
        List<Permission> permissions = permissionRepository.findByUserId(adminUser.get().getId());

        // Then
        assertThat(permissions).isNotEmpty();
        assertThat(permissions).anyMatch(p -> p.getName().equals("USER_READ"));
    }

    @Test
    @DisplayName("権限名の存在チェックができる")
    void existsByName_ShouldReturnTrue_WhenPermissionExists() {
        // When
        boolean exists = permissionRepository.existsByName("USER_WRITE");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("新しい権限を保存できる")
    void save_ShouldSaveNewPermission() {
        // Given
        Permission newPermission = new Permission(
            "CONTENT_READ",
            "CONTENT",
            "READ",
            "コンテンツの読み取り"
        );

        // When
        Permission savedPermission = permissionRepository.save(newPermission);

        // Then
        assertThat(savedPermission.getId()).isNotNull();
        assertThat(savedPermission.getName()).isEqualTo("CONTENT_READ");
        assertThat(savedPermission.getResource()).isEqualTo("CONTENT");
        assertThat(savedPermission.getAction()).isEqualTo("READ");
        assertThat(savedPermission.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("全てのデフォルト権限が作成されている")
    void shouldHaveDefaultPermissions() {
        // When
        Iterable<Permission> allPermissions = permissionRepository.findAll();

        // Then
        assertThat(allPermissions)
            .extracting(Permission::getName)
            .contains("USER_READ", "USER_WRITE", "USER_DELETE", 
                     "ROLE_READ", "PERMISSION_READ", "AUDIT_READ");
    }
}
