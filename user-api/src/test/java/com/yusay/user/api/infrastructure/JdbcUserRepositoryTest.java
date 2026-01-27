package com.yusay.user.api.infrastructure;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("JdbcUserRepository のテスト")
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcUserRepository jdbcUserRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUp() {
        // テストデータのクリーンアップ
        jdbcClient.sql("DELETE FROM users").update();
    }

    @Test
    @DisplayName("findById: ユーザーが存在する場合、Userを返す")
    void findById_whenUserExists_returnsUser() {
        // Given: テストユーザーを挿入
        String userId = "test-user-id-001";
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "$2a$10$test-password-hash";
        LocalDateTime now = LocalDateTime.now();

        jdbcClient.sql("""
                INSERT INTO users (id, username, email, password_hash, enabled,
                                   account_non_expired, account_non_locked, credentials_non_expired,
                                   created_at, updated_at)
                VALUES (:id, :username, :email, :passwordHash, :enabled,
                        :accountNonExpired, :accountNonLocked, :credentialsNonExpired,
                        :createdAt, :updatedAt)
                """)
                .param("id", userId)
                .param("username", username)
                .param("email", email)
                .param("passwordHash", passwordHash)
                .param("enabled", true)
                .param("accountNonExpired", true)
                .param("accountNonLocked", true)
                .param("credentialsNonExpired", true)
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

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
    @DisplayName("findById: 複数のユーザーが存在する場合、指定したIDのユーザーのみを返す")
    void findById_whenMultipleUsersExist_returnsOnlySpecifiedUser() {
        // Given: 複数のテストユーザーを挿入
        String userId1 = "test-user-id-001";
        String userId2 = "test-user-id-002";
        LocalDateTime now = LocalDateTime.now();

        jdbcClient.sql("""
                INSERT INTO users (id, username, email, password_hash, enabled,
                                   account_non_expired, account_non_locked, credentials_non_expired,
                                   created_at, updated_at)
                VALUES (:id, :username, :email, :passwordHash, true, true, true, true, :createdAt, :updatedAt)
                """)
                .param("id", userId1)
                .param("username", "user1")
                .param("email", "user1@example.com")
                .param("passwordHash", "$2a$10$hash1")
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

        jdbcClient.sql("""
                INSERT INTO users (id, username, email, password_hash, enabled,
                                   account_non_expired, account_non_locked, credentials_non_expired,
                                   created_at, updated_at)
                VALUES (:id, :username, :email, :passwordHash, true, true, true, true, :createdAt, :updatedAt)
                """)
                .param("id", userId2)
                .param("username", "user2")
                .param("email", "user2@example.com")
                .param("passwordHash", "$2a$10$hash2")
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

        // When: userId1でfindByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId1);

        // Then: userId1のユーザーのみが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(userId1);
        assertThat(result.get().username()).isEqualTo("user1");
        assertThat(result.get().email()).isEqualTo("user1@example.com");
    }

    @Test
    @DisplayName("findById: enabledがfalseのユーザーも取得できる")
    void findById_whenUserIsDisabled_returnsUser() {
        // Given: enabledがfalseのテストユーザーを挿入
        String userId = "disabled-user-id";
        LocalDateTime now = LocalDateTime.now();

        jdbcClient.sql("""
                INSERT INTO users (id, username, email, password_hash, enabled,
                                   account_non_expired, account_non_locked, credentials_non_expired,
                                   created_at, updated_at)
                VALUES (:id, :username, :email, :passwordHash, :enabled, true, true, true, :createdAt, :updatedAt)
                """)
                .param("id", userId)
                .param("username", "disableduser")
                .param("email", "disabled@example.com")
                .param("passwordHash", "$2a$10$disabled-hash")
                .param("enabled", false)
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId);

        // Then: enabledがfalseのユーザーが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().enabled()).isFalse();
    }
}
