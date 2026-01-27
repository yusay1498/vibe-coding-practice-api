package com.yusay.user.api.infrastructure;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcUserRepository.class})
@DisplayName("JdbcUserRepository のテスト")
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcUserRepository jdbcUserRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Test
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-user-id-001', 'testuser', 'test@example.com', '$2a$10$test-password-hash',
                    true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
            """
    })
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
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-user-id-001', 'user1', 'user1@example.com', '$2a$10$hash1',
                    true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
            """,
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-user-id-002', 'user2', 'user2@example.com', '$2a$10$hash2',
                    true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
            """
    })
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
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('disabled-user-id', 'disableduser', 'disabled@example.com', '$2a$10$disabled-hash',
                    false, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
            """
    })
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

    @Test
    @DisplayName("findById: nullのIDを渡した場合、空のOptionalを返す")
    void findById_whenIdIsNull_returnsEmptyOptional() {
        // Given: nullのユーザーID
        String nullUserId = null;

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(nullUserId);

        // Then: 空のOptionalが返されることを確認
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById: 空文字列のIDを渡した場合、空のOptionalを返す")
    void findById_whenIdIsEmpty_returnsEmptyOptional() {
        // Given: 空文字列のユーザーID
        String emptyUserId = "";

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(emptyUserId);

        // Then: 空のOptionalが返されることを確認
        assertThat(result).isEmpty();
    }
}
