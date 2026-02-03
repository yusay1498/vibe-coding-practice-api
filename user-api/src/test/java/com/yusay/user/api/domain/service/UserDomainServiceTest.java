package com.yusay.user.api.domain.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserDomainService のテスト")
class UserDomainServiceTest {

    @Test
    @DisplayName("createUser()は作成日時と更新日時を現在時刻に設定する")
    void createUser_SetsCreatedAtAndUpdatedAtToNow() {
        // Arrange
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        LocalDateTime expectedTime = LocalDateTime.ofInstant(fixedInstant, ZoneId.systemDefault());
        
        UserDomainService service = new UserDomainService(fixedClock);
        
        // Act
        User user = service.createUser(
                "test-id",
                "testuser",
                "test@example.com",
                "hashedPassword",
                true,
                true,
                true,
                true
        );
        
        // Assert
        assertThat(user.id()).isEqualTo("test-id");
        assertThat(user.username()).isEqualTo("testuser");
        assertThat(user.email()).isEqualTo("test@example.com");
        assertThat(user.passwordHash()).isEqualTo("hashedPassword");
        assertThat(user.enabled()).isTrue();
        assertThat(user.accountNonExpired()).isTrue();
        assertThat(user.accountNonLocked()).isTrue();
        assertThat(user.credentialsNonExpired()).isTrue();
        
        // 作成日時と更新日時が固定時刻に設定されていることを確認
        assertThat(user.createdAt()).isEqualTo(expectedTime);
        assertThat(user.updatedAt()).isEqualTo(expectedTime);
    }

    @Test
    @DisplayName("createUser()はIDがnullでも動作する")
    void createUser_WorksWithNullId() {
        // Arrange
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault());
        UserDomainService service = new UserDomainService(fixedClock);
        
        // Act
        User user = service.createUser(
                null,
                "testuser",
                "test@example.com",
                "hashedPassword",
                true,
                true,
                true,
                true
        );
        
        // Assert
        assertThat(user.id()).isNull();
        assertThat(user.username()).isEqualTo("testuser");
        assertThat(user.createdAt()).isNotNull();
        assertThat(user.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateUser()は更新日時を現在時刻に設定し、作成日時は保持する")
    void updateUser_UpdatesUpdatedAtAndPreservesCreatedAt() {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User originalUser = new User(
                "test-id",
                "oldusername",
                "old@example.com",
                "oldPasswordHash",
                true,
                true,
                true,
                true,
                originalCreatedAt,
                originalUpdatedAt
        );
        
        // 更新時刻を固定
        Instant updateInstant = Instant.parse("2024-01-02T15:30:00Z");
        Clock updateClock = Clock.fixed(updateInstant, ZoneId.systemDefault());
        LocalDateTime expectedUpdatedAt = LocalDateTime.ofInstant(updateInstant, ZoneId.systemDefault());
        
        UserDomainService service = new UserDomainService(updateClock);
        
        // Act
        User updatedUser = service.updateUser(
                originalUser,
                "newusername",
                "new@example.com",
                "newPasswordHash",
                false,
                false,
                false,
                false
        );
        
        // Assert
        assertThat(updatedUser.id()).isEqualTo("test-id");
        assertThat(updatedUser.username()).isEqualTo("newusername");
        assertThat(updatedUser.email()).isEqualTo("new@example.com");
        assertThat(updatedUser.passwordHash()).isEqualTo("newPasswordHash");
        assertThat(updatedUser.enabled()).isFalse();
        assertThat(updatedUser.accountNonExpired()).isFalse();
        assertThat(updatedUser.accountNonLocked()).isFalse();
        assertThat(updatedUser.credentialsNonExpired()).isFalse();
        
        // 作成日時は元のまま保持されていることを確認
        assertThat(updatedUser.createdAt()).isEqualTo(originalCreatedAt);
        
        // 更新日時が固定時刻に更新されていることを確認
        assertThat(updatedUser.updatedAt()).isEqualTo(expectedUpdatedAt);
        assertThat(updatedUser.updatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("updateUser()はnullフィールドを既存値で保持する")
    void updateUser_PreservesExistingValuesForNullFields() {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User originalUser = new User(
                "test-id",
                "existingusername",
                "existing@example.com",
                "existingPasswordHash",
                true,
                true,
                true,
                true,
                originalCreatedAt,
                originalUpdatedAt
        );
        
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-02T10:00:00Z"), ZoneId.systemDefault());
        UserDomainService service = new UserDomainService(fixedClock);
        
        // Act - usernameのみ更新、他はnull
        User updatedUser = service.updateUser(
                originalUser,
                "newusername",
                null,  // emailは更新しない
                null,  // passwordHashは更新しない
                null,  // enabledは更新しない
                null,
                null,
                null
        );
        
        // Assert
        assertThat(updatedUser.username()).isEqualTo("newusername");
        assertThat(updatedUser.email()).isEqualTo("existing@example.com");
        assertThat(updatedUser.passwordHash()).isEqualTo("existingPasswordHash");
        assertThat(updatedUser.enabled()).isTrue();
        assertThat(updatedUser.accountNonExpired()).isTrue();
        assertThat(updatedUser.accountNonLocked()).isTrue();
        assertThat(updatedUser.credentialsNonExpired()).isTrue();
        assertThat(updatedUser.createdAt()).isEqualTo(originalCreatedAt);
        assertThat(updatedUser.updatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("updateUser()は全てnullの場合でも更新日時のみ更新する")
    void updateUser_UpdatesOnlyUpdatedAtWhenAllFieldsAreNull() {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        User originalUser = new User(
                "test-id",
                "username",
                "email@example.com",
                "passwordHash",
                true,
                true,
                true,
                true,
                originalCreatedAt,
                originalUpdatedAt
        );
        
        Instant updateInstant = Instant.parse("2024-01-02T15:00:00Z");
        Clock updateClock = Clock.fixed(updateInstant, ZoneId.systemDefault());
        LocalDateTime expectedUpdatedAt = LocalDateTime.ofInstant(updateInstant, ZoneId.systemDefault());
        
        UserDomainService service = new UserDomainService(updateClock);
        
        // Act - 全てnull
        User updatedUser = service.updateUser(originalUser, null, null, null, null, null, null, null);
        
        // Assert - 全ての値が保持されている
        assertThat(updatedUser.id()).isEqualTo("test-id");
        assertThat(updatedUser.username()).isEqualTo("username");
        assertThat(updatedUser.email()).isEqualTo("email@example.com");
        assertThat(updatedUser.passwordHash()).isEqualTo("passwordHash");
        assertThat(updatedUser.enabled()).isTrue();
        assertThat(updatedUser.accountNonExpired()).isTrue();
        assertThat(updatedUser.accountNonLocked()).isTrue();
        assertThat(updatedUser.credentialsNonExpired()).isTrue();
        assertThat(updatedUser.createdAt()).isEqualTo(originalCreatedAt);
        
        // 更新日時のみ更新されている
        assertThat(updatedUser.updatedAt()).isEqualTo(expectedUpdatedAt);
        assertThat(updatedUser.updatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("validateDeleteAll()は削除対象が上限以下の場合は正常に完了する")
    void validateDeleteAll_Succeeds_WhenWithinLimit() {
        // Arrange
        Clock clock = Clock.systemUTC();
        UserDomainService service = new UserDomainService(clock);
        
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(new User("id" + i, "user" + i, "user" + i + "@example.com",
                "hash", true, true, true, true, LocalDateTime.now(), LocalDateTime.now()));
        }
        
        // Act & Assert - 例外がスローされないことを確認
        service.validateDeleteAll(users, 100);
    }

    @Test
    @DisplayName("validateDeleteAll()は削除対象が上限と同じ場合は正常に完了する")
    void validateDeleteAll_Succeeds_WhenExactlyAtLimit() {
        // Arrange
        Clock clock = Clock.systemUTC();
        UserDomainService service = new UserDomainService(clock);
        
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(new User("id" + i, "user" + i, "user" + i + "@example.com",
                "hash", true, true, true, true, LocalDateTime.now(), LocalDateTime.now()));
        }
        
        // Act & Assert - 例外がスローされないことを確認
        service.validateDeleteAll(users, 100);
    }

    @Test
    @DisplayName("validateDeleteAll()は削除対象が上限を超える場合に例外をスローする")
    void validateDeleteAll_ThrowsException_WhenExceedsLimit() {
        // Arrange
        Clock clock = Clock.systemUTC();
        UserDomainService service = new UserDomainService(clock);
        
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            users.add(new User("id" + i, "user" + i, "user" + i + "@example.com",
                "hash", true, true, true, true, LocalDateTime.now(), LocalDateTime.now()));
        }
        
        // Act & Assert
        assertThatThrownBy(() -> service.validateDeleteAll(users, 100))
            .isInstanceOf(DeleteAllNotAllowedException.class)
            .hasMessageContaining("削除対象ユーザー数（101件）が上限（100件）を超えています");
    }

    @Test
    @DisplayName("validateDeleteAll()は空のリストの場合は正常に完了する")
    void validateDeleteAll_Succeeds_WhenEmptyList() {
        // Arrange
        Clock clock = Clock.systemUTC();
        UserDomainService service = new UserDomainService(clock);
        
        // Act & Assert - 例外がスローされないことを確認
        service.validateDeleteAll(List.of(), 100);
    }

    @Test
    @DisplayName("validateDeleteAll()はmaxAllowedDeletionsが0以下の場合に例外をスローする")
    void validateDeleteAll_ThrowsException_WhenMaxAllowedDeletionsIsInvalid() {
        // Arrange
        Clock clock = Clock.systemUTC();
        UserDomainService service = new UserDomainService(clock);
        List<User> users = List.of(
            new User("id1", "user1", "user1@example.com", "hash", true, true, true, true, 
                LocalDateTime.now(), LocalDateTime.now())
        );
        
        // Act & Assert - 0の場合
        assertThatThrownBy(() -> service.validateDeleteAll(users, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("maxAllowedDeletions must be positive");
        
        // Act & Assert - 負の数の場合
        assertThatThrownBy(() -> service.validateDeleteAll(users, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("maxAllowedDeletions must be positive");
    }

    @Test
    @DisplayName("getCurrentTime()は現在時刻を返す")
    void getCurrentTime_ReturnsCurrentTime() {
        // Arrange
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        LocalDateTime expectedTime = LocalDateTime.ofInstant(fixedInstant, ZoneId.systemDefault());
        UserDomainService service = new UserDomainService(fixedClock);
        
        // Act
        LocalDateTime result = service.getCurrentTime();
        
        // Assert
        assertThat(result).isEqualTo(expectedTime);
    }
}
