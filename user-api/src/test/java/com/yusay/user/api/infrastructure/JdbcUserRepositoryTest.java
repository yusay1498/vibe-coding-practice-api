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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
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
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
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
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """,
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-user-id-002', 'user2', 'user2@example.com', '$2a$10$hash2',
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
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
                    false, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
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

    @Test
    @DisplayName("save: 新規ユーザーを保存できる")
    void save_whenNewUser_insertsUser() {
        // Given: 新規ユーザー
        String userId = "new-user-id-001";
        User newUser = new User(
                userId,
                "newuser",
                "newuser@example.com",
                "$2a$10$new-password-hash",
                true,
                true,
                true,
                true,
                null,  // created_atはsave時に設定される
                null   // updated_atはsave時に設定される
        );

        // When: saveを実行
        User savedUser = jdbcUserRepository.save(newUser);

        // Then: ユーザーが保存されることを確認
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.id()).isEqualTo(userId);
        assertThat(savedUser.username()).isEqualTo("newuser");
        assertThat(savedUser.email()).isEqualTo("newuser@example.com");
        assertThat(savedUser.passwordHash()).isEqualTo("$2a$10$new-password-hash");
        assertThat(savedUser.enabled()).isTrue();
        assertThat(savedUser.createdAt()).isNotNull();
        assertThat(savedUser.updatedAt()).isNotNull();

        // Then: データベースから取得できることを確認
        Optional<User> retrievedUser = jdbcUserRepository.findById(userId);
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().username()).isEqualTo("newuser");
    }

    @Test
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('existing-user-id', 'existinguser', 'existing@example.com', '$2a$10$existing-hash',
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """
    })
    @DisplayName("save: 既存ユーザーを更新できる")
    void save_whenExistingUser_updatesUser() {
        // Given: 既存のユーザーIDで更新内容を作成
        String userId = "existing-user-id";
        User updatedUser = new User(
                userId,
                "updateduser",
                "updated@example.com",
                "$2a$10$updated-password-hash",
                false,
                false,
                false,
                false,
                null,  // created_atは変更されない
                null   // updated_atはsave時に更新される
        );

        // When: saveを実行
        User savedUser = jdbcUserRepository.save(updatedUser);

        // Then: ユーザーが更新されることを確認
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.id()).isEqualTo(userId);
        assertThat(savedUser.username()).isEqualTo("updateduser");
        assertThat(savedUser.email()).isEqualTo("updated@example.com");
        assertThat(savedUser.passwordHash()).isEqualTo("$2a$10$updated-password-hash");
        assertThat(savedUser.enabled()).isFalse();
        assertThat(savedUser.accountNonExpired()).isFalse();
        assertThat(savedUser.accountNonLocked()).isFalse();
        assertThat(savedUser.credentialsNonExpired()).isFalse();
        // created_atが保持されていることを確認（元の2024-01-01のまま）
        assertThat(savedUser.createdAt()).isNotNull();
        assertThat(savedUser.createdAt().getYear()).isEqualTo(2024);
        assertThat(savedUser.createdAt().getMonthValue()).isEqualTo(1);
        assertThat(savedUser.createdAt().getDayOfMonth()).isEqualTo(1);
        // updated_atが更新されていることを確認
        assertThat(savedUser.updatedAt()).isNotNull();
        assertThat(savedUser.updatedAt()).isAfter(savedUser.createdAt());

        // Then: データベースから取得して更新を確認
        Optional<User> retrievedUser = jdbcUserRepository.findById(userId);
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().username()).isEqualTo("updateduser");
        assertThat(retrievedUser.get().email()).isEqualTo("updated@example.com");
        // created_atが保持されていることを再確認
        assertThat(retrievedUser.get().createdAt().getYear()).isEqualTo(2024);
    }

    @Test
    @DisplayName("save: enabledがfalseの新規ユーザーを保存できる")
    void save_whenNewDisabledUser_insertsUser() {
        // Given: enabledがfalseの新規ユーザー
        String userId = "disabled-new-user-id";
        User newUser = new User(
                userId,
                "disableduser",
                "disabled@example.com",
                "$2a$10$disabled-hash",
                false,
                true,
                true,
                true,
                null,
                null
        );

        // When: saveを実行
        User savedUser = jdbcUserRepository.save(newUser);

        // Then: enabledがfalseで保存されることを確認
        assertThat(savedUser.enabled()).isFalse();

        // Then: データベースから取得してenabledがfalseを確認
        Optional<User> retrievedUser = jdbcUserRepository.findById(userId);
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().enabled()).isFalse();
    }

    @Test
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('update-test-user-id', 'originaluser', 'original@example.com', '$2a$10$original-hash',
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """
    })
    @DisplayName("save: 既存ユーザーの一部の項目のみを更新できる")
    void save_whenExistingUserPartialUpdate_updatesOnlySpecifiedFields() {
        // Given: 既存のユーザーを取得
        String userId = "update-test-user-id";
        Optional<User> existingUser = jdbcUserRepository.findById(userId);
        assertThat(existingUser).isPresent();

        // Given: 一部の項目を変更したユーザー（usernameとemailのみ変更）
        User updatedUser = new User(
                userId,
                "changedusername",
                "changed@example.com",
                existingUser.get().passwordHash(),
                existingUser.get().enabled(),
                existingUser.get().accountNonExpired(),
                existingUser.get().accountNonLocked(),
                existingUser.get().credentialsNonExpired(),
                existingUser.get().createdAt(),
                existingUser.get().updatedAt()
        );

        // When: saveを実行
        User savedUser = jdbcUserRepository.save(updatedUser);

        // Then: usernameとemailが更新されることを確認
        assertThat(savedUser.username()).isEqualTo("changedusername");
        assertThat(savedUser.email()).isEqualTo("changed@example.com");
        assertThat(savedUser.passwordHash()).isEqualTo("$2a$10$original-hash");
    }
}
