package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import com.yusay.user.api.presentation.dto.CreateUserRequest;
import com.yusay.user.api.presentation.dto.UpdateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    @DisplayName("lookup()はIDに対応するユーザーを返す")
    void lookup_ReturnsUser_WhenUserExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "test-user-id";
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        User expectedUser = new User(
                userId,
                "testuser",
                "test@example.com",
                "hashedPassword",
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        User actualUser = userService.lookup(userId);

        // Assert
        assertThat(actualUser).isEqualTo(expectedUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("lookup()はユーザーが見つからない場合にUserNotFoundExceptionをスローする")
    void lookup_ThrowsUserNotFoundException_WhenUserNotFound() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "non-existent-id";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.lookup(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("list()は全ユーザーのリストを返す")
    void list_ReturnsAllUsers() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        List<User> expectedUsers = List.of(
                new User(
                        "user-id-1",
                        "user1",
                        "user1@example.com",
                        "hashedPassword1",
                        true,
                        true,
                        true,
                        true,
                        fixedDateTime,
                        fixedDateTime
                ),
                new User(
                        "user-id-2",
                        "user2",
                        "user2@example.com",
                        "hashedPassword2",
                        true,
                        true,
                        true,
                        true,
                        fixedDateTime,
                        fixedDateTime
                )
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.list();

        // Assert
        assertThat(actualUsers).isEqualTo(expectedUsers);
        assertThat(actualUsers).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("list()はユーザーが存在しない場合に空のリストを返す")
    void list_ReturnsEmptyList_WhenNoUsersExist() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> actualUsers = userService.list();

        // Assert
        assertThat(actualUsers).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("delete()は存在するユーザーを削除する")
    void delete_DeletesUser_WhenUserExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "test-user-id";
        when(userRepository.deleteById(userId)).thenReturn(1);

        // Act
        userService.delete(userId);

        // Assert
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("delete()はユーザーが見つからない場合にUserNotFoundExceptionをスローする")
    void delete_ThrowsUserNotFoundException_WhenUserNotFound() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "non-existent-id";
        when(userRepository.deleteById(userId)).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("create()は新しいユーザーを作成する")
    void create_CreatesNewUser() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "newuser@example.com",
                "password123"
        );
        
        String hashedPassword = "hashedPassword123";
        when(passwordEncoder.encode(request.password())).thenReturn(hashedPassword);
        
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        User savedUser = new User(
                "generated-id",
                request.username(),
                request.email(),
                hashedPassword,
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User createdUser = userService.create(request);

        // Assert
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.username()).isEqualTo(request.username());
        assertThat(createdUser.email()).isEqualTo(request.email());
        assertThat(createdUser.passwordHash()).isEqualTo(hashedPassword);
        assertThat(createdUser.enabled()).isTrue();
        assertThat(createdUser.accountNonExpired()).isTrue();
        assertThat(createdUser.accountNonLocked()).isTrue();
        assertThat(createdUser.credentialsNonExpired()).isTrue();
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("update()は既存ユーザーを更新する")
    void update_UpdatesExistingUser() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "test-user-id";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        User existingUser = new User(
                userId,
                "oldusername",
                "old@example.com",
                "oldHashedPassword",
                true,
                true,
                true,
                true,
                createdAt,
                createdAt
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        UpdateUserRequest request = new UpdateUserRequest(
                "newusername",
                "new@example.com",
                "newpassword123",
                false,
                null,
                null,
                null
        );
        
        String newHashedPassword = "newHashedPassword123";
        when(passwordEncoder.encode(request.password())).thenReturn(newHashedPassword);
        
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
        User updatedUser = new User(
                userId,
                request.username(),
                request.email(),
                newHashedPassword,
                request.enabled(),
                existingUser.accountNonExpired(),
                existingUser.accountNonLocked(),
                existingUser.credentialsNonExpired(),
                createdAt,
                updatedAt
        );
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.update(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(request.username());
        assertThat(result.email()).isEqualTo(request.email());
        assertThat(result.passwordHash()).isEqualTo(newHashedPassword);
        assertThat(result.enabled()).isEqualTo(request.enabled());
        assertThat(result.createdAt()).isEqualTo(createdAt);
        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("update()は部分的な更新をサポートする")
    void update_SupportsPartialUpdate() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "test-user-id";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        User existingUser = new User(
                userId,
                "existingusername",
                "existing@example.com",
                "existingHashedPassword",
                true,
                true,
                true,
                true,
                createdAt,
                createdAt
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        // usernameのみを更新するリクエスト
        UpdateUserRequest request = new UpdateUserRequest(
                "updatedusername",
                null,  // emailは更新しない
                null,  // passwordは更新しない
                null,
                null,
                null,
                null
        );
        
        User updatedUser = new User(
                userId,
                request.username(),
                existingUser.email(),  // 元のメールを維持
                existingUser.passwordHash(),  // 元のパスワードハッシュを維持
                existingUser.enabled(),
                existingUser.accountNonExpired(),
                existingUser.accountNonLocked(),
                existingUser.credentialsNonExpired(),
                createdAt,
                LocalDateTime.now()
        );
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.update(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(request.username());
        assertThat(result.email()).isEqualTo(existingUser.email());
        assertThat(result.passwordHash()).isEqualTo(existingUser.passwordHash());
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("update()は存在しないユーザーに対してUserNotFoundExceptionをスローする")
    void update_ThrowsUserNotFoundException_WhenUserNotFound() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, passwordEncoder);
        
        String userId = "non-existent-id";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        UpdateUserRequest request = new UpdateUserRequest(
                "newusername",
                "new@example.com",
                null,
                null,
                null,
                null,
                null
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.update(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository).findById(userId);
    }
}
