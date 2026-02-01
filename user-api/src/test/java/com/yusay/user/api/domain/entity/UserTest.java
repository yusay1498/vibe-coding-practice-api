package com.yusay.user.api.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User エンティティのテスト")
class UserTest {

    @Test
    @DisplayName("create()メソッドは作成日時と更新日時を現在時刻に設定する")
    void create_SetsCreatedAtAndUpdatedAtToNow() {
        // Arrange
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        LocalDateTime expectedTime = LocalDateTime.ofInstant(fixedInstant, ZoneId.systemDefault());
        
        // Act
        User user = User.create(
                "test-id",
                "testuser",
                "test@example.com",
                "hashedPassword",
                true,
                true,
                true,
                true,
                fixedClock
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
    @DisplayName("create()メソッド（Clock引数なし）はシステムクロックを使用する")
    void create_WithoutClock_UsesSystemClock() {
        // Arrange
        LocalDateTime beforeCreate = LocalDateTime.now();
        
        // Act
        User user = User.create(
                "test-id",
                "testuser",
                "test@example.com",
                "hashedPassword",
                true,
                true,
                true,
                true
        );
        
        LocalDateTime afterCreate = LocalDateTime.now();
        
        // Assert
        assertThat(user.createdAt()).isNotNull();
        assertThat(user.createdAt()).isBetween(beforeCreate, afterCreate);
        assertThat(user.updatedAt()).isNotNull();
        assertThat(user.updatedAt()).isBetween(beforeCreate, afterCreate);
    }

    @Test
    @DisplayName("create()メソッドはIDがnullでも動作する")
    void create_WorksWithNullId() {
        // Arrange
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault());
        
        // Act
        User user = User.create(
                null,
                "testuser",
                "test@example.com",
                "hashedPassword",
                true,
                true,
                true,
                true,
                fixedClock
        );
        
        // Assert
        assertThat(user.id()).isNull();
        assertThat(user.username()).isEqualTo("testuser");
        assertThat(user.createdAt()).isNotNull();
        assertThat(user.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("update()メソッドは更新日時を現在時刻に設定し、作成日時は保持する")
    void update_UpdatesUpdatedAtAndPreservesCreatedAt() {
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
        
        // Act
        User updatedUser = originalUser.update(
                "newusername",
                "new@example.com",
                "newPasswordHash",
                false,
                false,
                false,
                false,
                updateClock
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
    @DisplayName("update()メソッド（Clock引数なし）はシステムクロックを使用する")
    void update_WithoutClock_UsesSystemClock() {
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
        
        LocalDateTime beforeUpdate = LocalDateTime.now();
        
        // Act
        User updatedUser = originalUser.update(
                "newusername",
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        LocalDateTime afterUpdate = LocalDateTime.now();
        
        // Assert
        assertThat(updatedUser.updatedAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(updatedUser.createdAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("update()メソッドはnullフィールドを既存値で保持する")
    void update_PreservesExistingValuesForNullFields() {
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
        
        // Act - usernameのみ更新、他はnull
        User updatedUser = originalUser.update(
                "newusername",
                null,  // emailは更新しない
                null,  // passwordHashは更新しない
                null,  // enabledは更新しない
                null,
                null,
                null,
                fixedClock
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
    @DisplayName("update()メソッドは全てnullの場合でも更新日時のみ更新する")
    void update_UpdatesOnlyUpdatedAtWhenAllFieldsAreNull() {
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
        
        // Act - 全てnull
        User updatedUser = originalUser.update(null, null, null, null, null, null, null, updateClock);
        
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
}
