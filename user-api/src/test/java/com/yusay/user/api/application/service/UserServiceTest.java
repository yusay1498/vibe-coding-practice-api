package com.yusay.user.api.application.service;

import com.yusay.user.api.application.dto.DeleteAllResult;
import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import com.yusay.user.api.domain.exception.DuplicateUserException;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import com.yusay.user.api.domain.service.UserDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
        UserService userService = new UserService(userRepository, userDomainService, "dev", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userDomainService.createUser(null, username, email, passwordHash, true, true, true, true))
                .thenReturn(newUser);
        
        // saveで制約違反が発生（競合状態）
        when(userRepository.save(newUser))
                .thenThrow(new DataIntegrityViolationException("UNIQUE constraint violation"));

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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
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
        UserService userService = new UserService(userRepository, userDomainService, "default", 1000);
        
        String userId = "non-existent-id";
        when(userRepository.deleteById(userId)).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("deleteAll()は開発環境で全ユーザーを削除して結果を返す")
    void deleteAll_DeletesAllUsers_AndReturnsResult_InDevelopmentEnvironment() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        LocalDateTime expectedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        UserService userService = new UserService(userRepository, userDomainService, "dev", 1000);
        
        List<User> users = List.of(
            new User("id1", "user1", "user1@example.com", "hash1", true, true, true, true, 
                LocalDateTime.now(), LocalDateTime.now()),
            new User("id2", "user2", "user2@example.com", "hash2", true, true, true, true, 
                LocalDateTime.now(), LocalDateTime.now())
        );
        
        when(userRepository.findAll()).thenReturn(users);
        doNothing().when(userDomainService).validateDeleteAll(anyList(), anyInt());
        when(userDomainService.getCurrentTime()).thenReturn(expectedTime);
        when(userRepository.deleteAll()).thenReturn(2);

        // Act
        DeleteAllResult result = userService.deleteAll();

        // Assert
        assertThat(result.deletedCount()).isEqualTo(2);
        assertThat(result.environment()).isEqualTo("dev");
        assertThat(result.executedAt()).isEqualTo(expectedTime);
        verify(userRepository).findAll();
        verify(userDomainService).validateDeleteAll(eq(users), eq(1000));
        verify(userDomainService).getCurrentTime();
        verify(userRepository).deleteAll();
    }

    @Test
    @DisplayName("deleteAll()はユーザーが存在しない場合に0件削除を返す")
    void deleteAll_ReturnsZero_WhenNoUsersExist() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService, "test", 1000);
        
        when(userRepository.findAll()).thenReturn(List.of());
        doNothing().when(userDomainService).validateDeleteAll(anyList(), anyInt());
        when(userRepository.deleteAll()).thenReturn(0);

        // Act
        DeleteAllResult result = userService.deleteAll();

        // Assert
        assertThat(result.deletedCount()).isEqualTo(0);
        assertThat(result.environment()).isEqualTo("test");
        verify(userRepository).deleteAll();
    }

    @Test
    @DisplayName("deleteAll()は本番環境では例外をスローする")
    void deleteAll_ThrowsException_InProductionEnvironment() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService, "prod", 1000);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteAll())
                .isInstanceOf(DeleteAllNotAllowedException.class)
                .hasMessageContaining("本番環境では全件削除を実行できません");
        
        verify(userRepository, never()).deleteAll();
    }

    @Test
    @DisplayName("deleteAll()は削除件数が上限を超える場合に例外をスローする")
    void deleteAll_ThrowsException_WhenDeletionLimitExceeded() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService, "dev", 1000);
        
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(new User("id" + i, "user" + i, "user" + i + "@example.com", 
                "hash", true, true, true, true, LocalDateTime.now(), LocalDateTime.now()));
        }
        
        when(userRepository.findAll()).thenReturn(users);
        doThrow(new DeleteAllNotAllowedException("削除対象ユーザー数が上限を超えています"))
            .when(userDomainService).validateDeleteAll(anyList(), anyInt());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteAll())
                .isInstanceOf(DeleteAllNotAllowedException.class)
                .hasMessageContaining("削除対象ユーザー数が上限を超えています");
        
        verify(userRepository, never()).deleteAll();
    }

    @Test
    @DisplayName("deleteAll()は削除後に削除件数が上限を超えた場合に例外をスローする（競合状態の検出）")
    void deleteAll_ThrowsException_WhenPostDeletionCountExceedsLimit() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        int maxLimit = 100;
        UserService userService = new UserService(userRepository, userDomainService, "dev", maxLimit);
        
        // 事前検証では上限以下のユーザーが存在
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            users.add(new User("id" + i, "user" + i, "user" + i + "@example.com", 
                "hash", true, true, true, true, LocalDateTime.now(), LocalDateTime.now()));
        }
        
        when(userRepository.findAll()).thenReturn(users);
        doNothing().when(userDomainService).validateDeleteAll(anyList(), anyInt());
        // しかし実際には上限を超える数が削除された（競合状態）
        when(userRepository.deleteAll()).thenReturn(150);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteAll())
                .isInstanceOf(DeleteAllNotAllowedException.class)
                .hasMessageContaining("削除件数（150件）が上限（100件）を超えているため");
        
        verify(userRepository).deleteAll();
    }

    @Test
    @DisplayName("deleteAll()は複数プロファイルが設定されている場合でも本番環境を正しく検出する")
    void deleteAll_ThrowsException_WithMultipleProfiles() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService, "dev,prod,debug", 1000);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteAll())
                .isInstanceOf(DeleteAllNotAllowedException.class)
                .hasMessageContaining("本番環境では全件削除を実行できません");
        
        verify(userRepository, never()).deleteAll();
    }

    @Test
    @DisplayName("deleteAll()は'production'プロファイルでも例外をスローする")
    void deleteAll_ThrowsException_WithProductionProfile() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);
        UserService userService = new UserService(userRepository, userDomainService, "production", 1000);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteAll())
                .isInstanceOf(DeleteAllNotAllowedException.class)
                .hasMessageContaining("本番環境では全件削除を実行できません");
        
        verify(userRepository, never()).deleteAll();
    }

    @Test
    @DisplayName("UserService()はmaxAllowedDeletionsが0以下の場合に例外をスローする")
    void constructor_ThrowsException_WhenMaxAllowedDeletionsIsZeroOrNegative() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserDomainService userDomainService = mock(UserDomainService.class);

        // Act & Assert - 0の場合
        assertThatThrownBy(() -> new UserService(userRepository, userDomainService, "dev", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxAllowedDeletions must be positive");

        // Act & Assert - 負の数の場合
        assertThatThrownBy(() -> new UserService(userRepository, userDomainService, "dev", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxAllowedDeletions must be positive");
    }
}
