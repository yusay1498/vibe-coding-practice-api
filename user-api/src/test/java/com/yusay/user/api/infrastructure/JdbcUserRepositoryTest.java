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
import java.util.List;
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
        LocalDateTime now = LocalDateTime.now();
        User newUser = new User(
                userId,
                "newuser",
                "newuser@example.com",
                "$2a$10$new-password-hash",
                true,
                true,
                true,
                true,
                now,  // created_atを設定
                now   // updated_atを設定
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
    @DisplayName("findAll: ユーザーが存在しない場合、空のリストを返す")
    void findAll_whenNoUsersExist_returnsEmptyList() {
        // Given: ユーザーが存在しない状態

        // When: findAllを実行
        List<User> result = jdbcUserRepository.findAll();

        // Then: 空のリストが返されることを確認
        assertThat(result).isEmpty();
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
        LocalDateTime now = LocalDateTime.now();
        User updatedUser = new User(
                userId,
                "updateduser",
                "updated@example.com",
                "$2a$10$updated-password-hash",
                false,
                false,
                false,
                false,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),  // created_atは変更されない
                now   // updated_atを設定
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
        // updated_atが更新されていることを確認（元の2024-01-01より後であることを確認）
        assertThat(savedUser.updatedAt()).isNotNull();
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        assertThat(savedUser.updatedAt()).isAfter(originalUpdatedAt);

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
        LocalDateTime now = LocalDateTime.now();
        User newUser = new User(
                userId,
                "disableduser",
                "disabled@example.com",
                "$2a$10$disabled-hash",
                false,
                true,
                true,
                true,
                now,
                now
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
            VALUES ('test-delete-user-001', 'deleteuser', 'delete@example.com', '$2a$10$test-hash',
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """
    })
    @DisplayName("deleteById: ユーザーが存在する場合、削除される")
    void deleteById_whenUserExists_deletesUser() {
        // Given: テストユーザーを挿入
        String userId = "test-delete-user-001";

        // When: deleteByIdを実行
        int deletedCount = jdbcUserRepository.deleteById(userId);

        // Then: 削除されたレコード数が1であることを確認
        assertThat(deletedCount).isEqualTo(1);
        
        // Then: ユーザーが削除されていることを確認
        Optional<User> result = jdbcUserRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById: ユーザーが存在しない場合、例外をスローしない")
    void deleteById_whenUserDoesNotExist_doesNotThrowException() {
        // Given: 存在しないユーザーID
        String nonExistentUserId = "non-existent-user-id";

        // When: deleteByIdを実行
        int deletedCount = jdbcUserRepository.deleteById(nonExistentUserId);

        // Then: 削除されたレコード数が0であることを確認
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-delete-user-002', 'user1', 'user1@example.com', '$2a$10$hash1',
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """,
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-delete-user-003', 'user2', 'user2@example.com', '$2a$10$hash2',
                    true, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """
    })
    @DisplayName("deleteById: 複数のユーザーが存在する場合、指定したIDのユーザーのみを削除する")
    void deleteById_whenMultipleUsersExist_deletesOnlySpecifiedUser() {
        // Given: 複数のテストユーザーを挿入
        String userId1 = "test-delete-user-002";
        String userId2 = "test-delete-user-003";

        // When: userId1のユーザーを削除
        int deletedCount = jdbcUserRepository.deleteById(userId1);

        // Then: 削除されたレコード数が1であることを確認
        assertThat(deletedCount).isEqualTo(1);

        // Then: userId1のユーザーは削除され、userId2のユーザーは存在することを確認
        Optional<User> result1 = jdbcUserRepository.findById(userId1);
        Optional<User> result2 = jdbcUserRepository.findById(userId2);

        assertThat(result1).isEmpty();
        assertThat(result2).isPresent();
        assertThat(result2.get().id()).isEqualTo(userId2);
    }

    @Test
    @DisplayName("deleteById: nullのIDを渡した場合、例外をスローしない")
    void deleteById_whenIdIsNull_doesNotThrowException() {
        // Given: nullのユーザーID
        String nullUserId = null;

        // When: deleteByIdを実行
        int deletedCount = jdbcUserRepository.deleteById(nullUserId);

        // Then: 削除されたレコード数が0であることを確認
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("deleteById: 空文字列のIDを渡した場合、例外をスローしない")
    void deleteById_whenIdIsEmpty_doesNotThrowException() {
        // Given: 空文字列のユーザーID
        String emptyUserId = "";

        // When: deleteByIdを実行
        int deletedCount = jdbcUserRepository.deleteById(emptyUserId);

        // Then: 削除されたレコード数が0であることを確認
        assertThat(deletedCount).isEqualTo(0);
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
    @DisplayName("save: 既存ユーザー更新時に username/email のみ変更され、他のフィールドは保持される")
    void save_whenExistingUser_updatesSpecifiedFields() {
        // Given: 既存のユーザーを取得
        String userId = "update-test-user-id";
        Optional<User> existingUser = jdbcUserRepository.findById(userId);
        assertThat(existingUser).isPresent();

        // Given: usernameとemailのみを変更し、updated_atを現在時刻に設定したUserオブジェクトを作成
        LocalDateTime now = LocalDateTime.now();
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
                now  // updated_atを現在時刻に設定
        );

        // When: saveを実行
        User savedUser = jdbcUserRepository.save(updatedUser);

        // Then: usernameとemailが更新され、他のフィールドも保持されることを確認
        assertThat(savedUser.username()).isEqualTo("changedusername");
        assertThat(savedUser.email()).isEqualTo("changed@example.com");
        assertThat(savedUser.passwordHash()).isEqualTo("$2a$10$original-hash");
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
    @DisplayName("findAll: ユーザーが1件存在する場合、そのユーザーを含むリストを返す")
    void findAll_whenOneUserExists_returnsListWithOneUser() {
        // Given: テストユーザーを1件挿入
        String userId = "test-user-id-001";
        String username = "testuser";
        String email = "test@example.com";

        // When: findAllを実行
        List<User> result = jdbcUserRepository.findAll();

        // Then: 1件のユーザーが取得できることを確認
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(userId);
        assertThat(result.get(0).username()).isEqualTo(username);
        assertThat(result.get(0).email()).isEqualTo(email);
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
            """,
            """
            INSERT INTO users (id, username, email, password_hash, enabled,
                               account_non_expired, account_non_locked, credentials_non_expired,
                               created_at, updated_at)
            VALUES ('test-user-id-003', 'user3', 'user3@example.com', '$2a$10$hash3',
                    false, true, true, true, '2024-01-01 00:00:00', '2024-01-01 00:00:00');
            """
    })
    @DisplayName("findAll: 複数のユーザーが存在する場合、全てのユーザーを含むリストを返す")
    void findAll_whenMultipleUsersExist_returnsListWithAllUsers() {
        // Given: テストユーザーを3件挿入（1件はenabled=false）

        // When: findAllを実行
        List<User> result = jdbcUserRepository.findAll();

        // Then: 3件のユーザーが取得できることを確認
        assertThat(result).hasSize(3);
        assertThat(result).extracting(User::id)
                .containsExactlyInAnyOrder("test-user-id-001", "test-user-id-002", "test-user-id-003");
        assertThat(result).extracting(User::username)
                .containsExactlyInAnyOrder("user1", "user2", "user3");

        // enabledがfalseのユーザーも含まれることを確認
        assertThat(result).anyMatch(user -> !user.enabled());
    }
}
