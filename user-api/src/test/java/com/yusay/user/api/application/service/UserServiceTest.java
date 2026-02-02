package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DuplicateUserException;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import com.yusay.user.api.domain.service.UserDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    @DisplayName("create()は新規ユーザーを作成して返す")
    void create_CreatesAndReturnsNewUser_WhenNoConflicts() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
        String username = "newuser";
        String email = "newuser@example.com";
        String passwordHash = "hashedPassword";
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User domainServiceUser = new User(
                null,
                username,
                email,
                passwordHash,
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        
        User savedUser = new User(
                "generated-id",
                username,
                email,
                passwordHash,
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userDomainService.createUser(null, username, email, passwordHash, true, true, true, true))
                .thenReturn(domainServiceUser);
        when(userRepository.save(domainServiceUser)).thenReturn(savedUser);

        // Act
        User result = userService.create(username, email, passwordHash);

        // Assert
        assertThat(result).isEqualTo(savedUser);
        assertThat(result.id()).isEqualTo("generated-id");
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.email()).isEqualTo(email);
        
        verify(userRepository).findByEmail(email);
        verify(userRepository).findByUsername(username);
        verify(userDomainService).createUser(null, username, email, passwordHash, true, true, true, true);
        verify(userRepository).save(domainServiceUser);
    }

    @Test
    @DisplayName("create()はメールアドレスが既に存在する場合にDuplicateUserExceptionをスローする")
    void create_ThrowsDuplicateUserException_WhenEmailExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
        String username = "newuser";
        String email = "existing@example.com";
        String passwordHash = "hashedPassword";
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User existingUser = new User(
                "existing-id",
                "existinguser",
                email,
                "existingPasswordHash",
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.create(username, email, passwordHash))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("ユーザーが既に存在します: " + email);
        
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userDomainService, never()).createUser(any(), any(), any(), any(), any(), any(), any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create()はユーザー名が既に存在する場合にDuplicateUserExceptionをスローする")
    void create_ThrowsDuplicateUserException_WhenUsernameExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
        String username = "existinguser";
        String email = "newuser@example.com";
        String passwordHash = "hashedPassword";
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User existingUser = new User(
                "existing-id",
                username,
                "existing@example.com",
                "existingPasswordHash",
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.create(username, email, passwordHash))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("ユーザーが既に存在します: " + username);
        
        verify(userRepository).findByEmail(email);
        verify(userRepository).findByUsername(username);
        verify(userDomainService, never()).createUser(any(), any(), any(), any(), any(), any(), any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("create()はDB制約違反時に競合状態を適切に処理する")
    void create_HandlesRaceCondition_WhenDataIntegrityViolationOccurs() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
        String username = "newuser";
        String email = "newuser@example.com";
        String passwordHash = "hashedPassword";
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User newUser = new User(
                null,
                username,
                email,
                passwordHash,
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        
        User existingUser = new User(
                "existing-id",
                username,
                email,
                "existingPasswordHash",
                true,
                true,
                true,
                true,
                fixedDateTime,
                fixedDateTime
        );
        
        // 最初のチェックでは重複が見つからない
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userDomainService.createUser(null, username, email, passwordHash, true, true, true, true))
                .thenReturn(newUser);
        
        // saveで制約違反が発生（競合状態）
        when(userRepository.save(newUser))
                .thenThrow(new DataIntegrityViolationException("UNIQUE constraint violation"));
        
        // 再チェック時にユーザーが見つかる
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.create(username, email, passwordHash))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("ユーザーが既に存在します: " + email);
        
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("lookup()はIDに対応するユーザーを返す")
    void lookup_ReturnsUser_WhenUserExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
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
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
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
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
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
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
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
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
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
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService);
        
        String userId = "non-existent-id";
        when(userRepository.deleteById(userId)).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository).deleteById(userId);
    }
}
