package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    @DisplayName("lookup()はIDに対応するユーザーを返す")
    void lookup_ReturnsUser_WhenUserExists() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        
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
        UserService userService = new UserService(userRepository);
        
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
        UserService userService = new UserService(userRepository);
        
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
        UserService userService = new UserService(userRepository);
        
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
        UserService userService = new UserService(userRepository);
        
        String userId = "test-user-id";
        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        User existingUser = new User(
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
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userService.delete(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("delete()はユーザーが見つからない場合にUserNotFoundExceptionをスローする")
    void delete_ThrowsUserNotFoundException_WhenUserNotFound() {
        // Arrange
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        
        String userId = "non-existent-id";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository).findById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}
