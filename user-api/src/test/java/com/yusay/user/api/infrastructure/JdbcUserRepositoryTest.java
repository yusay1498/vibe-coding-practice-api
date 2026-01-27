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
    @DisplayName("findById: ユーザーが存在する場合、Userを返す")
    void findById_whenUserExists_returnsUser() {
        // Given: テストデータから提供されているユーザーID
        String userId = "750e8400-e29b-41d4-a716-446655440001";

        // When: findByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId);

        // Then: ユーザーが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(userId);
        assertThat(result.get().username()).isEqualTo("admin");
        assertThat(result.get().email()).isEqualTo("admin@example.com");
        assertThat(result.get().passwordHash()).isEqualTo("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
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
        // Given: テストデータから提供されている2つのユーザーID
        String userId1 = "750e8400-e29b-41d4-a716-446655440001";
        String userId2 = "750e8400-e29b-41d4-a716-446655440002";

        // When: userId1でfindByIdを実行
        Optional<User> result = jdbcUserRepository.findById(userId1);

        // Then: userId1のユーザーのみが取得できることを確認
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(userId1);
        assertThat(result.get().username()).isEqualTo("admin");
        assertThat(result.get().email()).isEqualTo("admin@example.com");
    }
}
