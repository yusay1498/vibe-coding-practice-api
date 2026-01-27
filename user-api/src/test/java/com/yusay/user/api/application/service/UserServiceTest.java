package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
}
