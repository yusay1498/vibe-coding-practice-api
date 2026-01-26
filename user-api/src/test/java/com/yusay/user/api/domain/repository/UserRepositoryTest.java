package com.yusay.user.api.domain.repository;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepositoryのテスト
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@DisplayName("ユーザーリポジトリのテスト")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("ユーザー名でユーザーを検索できる")
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // When
        Optional<User> user = userRepository.findByUsername("admin");

        // Then
        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo("admin");
        assertThat(user.get().getEmail()).isEqualTo("admin@example.com");
        assertThat(user.get().getEnabled()).isTrue();
    }

    @Test
    @DisplayName("存在しないユーザー名で検索した場合、Emptyが返る")
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // When
        Optional<User> user = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("メールアドレスでユーザーを検索できる")
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // When
        Optional<User> user = userRepository.findByEmail("user@example.com");

        // Then
        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo("user");
        assertThat(user.get().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("ユーザー名の存在チェックができる")
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // When
        boolean exists = userRepository.existsByUsername("admin");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("存在しないユーザー名の場合、falseが返る")
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("メールアドレスの存在チェックができる")
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // When
        boolean exists = userRepository.existsByEmail("admin@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("新しいユーザーを保存できる")
    void save_ShouldSaveNewUser() {
        // Given
        User newUser = new User("testuser", "test@example.com", "$2a$10$hashedpassword");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ユーザーを削除できる")
    void delete_ShouldDeleteUser() {
        // Given
        Optional<User> user = userRepository.findByUsername("user");
        assertThat(user).isPresent();

        // When
        userRepository.delete(user.get());

        // Then
        Optional<User> deletedUser = userRepository.findByUsername("user");
        assertThat(deletedUser).isEmpty();
    }
}
