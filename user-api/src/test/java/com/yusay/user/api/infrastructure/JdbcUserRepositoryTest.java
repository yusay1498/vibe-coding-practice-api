package com.yusay.user.api.infrastructure;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcUserRepository.class})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("JdbcUserRepository のテスト")
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcUserRepository jdbcUserRepository;

    @Test
    @Sql("/test-data-single-user.sql")
    @DisplayName("findById: ユーザーが存在する場合、Userを返す")
    void findById_whenUserExists_returnsUser() {
        // Given: テストユーザーを挿入
        String userId = "test-user-id-001";
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "$2a$10$test-password-hash";

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId);

        // Then: ユーザーが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(userId);
        assertThat(result.get().username()).isEqualTo(username);
        assertThat(result.get().email()).isEqualTo(email);
        assertThat(result.get().passwordHash()).isEqualTo(passwordHash);
        assertThat(result.get().enabled()).isTrue();
        assertThat(result.get().accountNonExpired()).isTrue();
        assertThat(result.get().accountNonLocked()).isTrue();
        assertThat(result.get().credentialsNonExpired()).isTrue();
        assertThat(result.get().createdAt()).isNotNull();
        assertThat(result.get().updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById: ユーザーが存在しない場合、空のOptionalを返す")
    void findById_whenUserDoesNotExist_returnsEmptyOptional() {
        // Given: 存在しないユーザーID
        String nonExistentUserId = "non-existent-user-id";

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(nonExistentUserId);

        // Then: 空のOptionalが返されることを確認
        assertThat(result).isEmpty();
    }

    @Test
    @Sql("/test-data-multiple-users.sql")
    @DisplayName("findById: 複数のユーザーが存在する場合、指定したIDのユーザーのみを返す")
    void findById_whenMultipleUsersExist_returnsOnlySpecifiedUser() {
        // Given: 複数のテストユーザーを挿入
        String userId1 = "test-user-id-001";
        String userId2 = "test-user-id-002";

        // When: userId1でfindByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId1);

        // Then: userId1のユーザーのみが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(userId1);
        assertThat(result.get().username()).isEqualTo("user1");
        assertThat(result.get().email()).isEqualTo("user1@example.com");
    }

    @Test
    @Sql("/test-data-disabled-user.sql")
    @DisplayName("findById: enabledがfalseのユーザーも取得できる")
    void findById_whenUserIsDisabled_returnsUser() {
        // Given: enabledがfalseのテストユーザーを挿入
        String userId = "disabled-user-id";

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId);

        // Then: enabledがfalseのユーザーが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().enabled()).isFalse();
    }
}
