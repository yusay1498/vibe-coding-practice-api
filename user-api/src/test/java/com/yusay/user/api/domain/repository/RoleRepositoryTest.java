package com.yusay.user.api.domain.repository;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.domain.model.Role;
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
 * RoleRepositoryのテスト
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@DisplayName("ロールリポジトリのテスト")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("ロール名でロールを検索できる")
    void findByName_ShouldReturnRole_WhenRoleExists() {
        // When
        Optional<Role> role = roleRepository.findByName("ROLE_ADMIN");

        // Then
        assertThat(role).isPresent();
        assertThat(role.get().getName()).isEqualTo("ROLE_ADMIN");
        assertThat(role.get().getDescription()).contains("システム管理者");
    }

    @Test
    @DisplayName("存在しないロール名で検索した場合、Emptyが返る")
    void findByName_ShouldReturnEmpty_WhenRoleDoesNotExist() {
        // When
        Optional<Role> role = roleRepository.findByName("ROLE_NONEXISTENT");

        // Then
        assertThat(role).isEmpty();
    }

    @Test
    @DisplayName("ユーザーIDに紐づくロール一覧を取得できる")
    void findByUserId_ShouldReturnRoles_WhenUserHasRoles() {
        // Given
        var adminUser = userRepository.findByUsername("admin");
        assertThat(adminUser).isPresent();

        // When
        List<Role> roles = roleRepository.findByUserId(adminUser.get().getId());

        // Then
        assertThat(roles).isNotEmpty();
        assertThat(roles).anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("ロール名の存在チェックができる")
    void existsByName_ShouldReturnTrue_WhenRoleExists() {
        // When
        boolean exists = roleRepository.existsByName("ROLE_USER");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("新しいロールを保存できる")
    void save_ShouldSaveNewRole() {
        // Given
        Role newRole = new Role("ROLE_GUEST", "ゲストユーザー");

        // When
        Role savedRole = roleRepository.save(newRole);

        // Then
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("ROLE_GUEST");
        assertThat(savedRole.getDescription()).isEqualTo("ゲストユーザー");
        assertThat(savedRole.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("全てのデフォルトロールが作成されている")
    void shouldHaveDefaultRoles() {
        // When
        Iterable<Role> allRoles = roleRepository.findAll();

        // Then
        assertThat(allRoles)
            .extracting(Role::getName)
            .contains("ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR");
    }
}
