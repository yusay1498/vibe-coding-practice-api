package com.yusay.user.api.presentation.controller;

import com.yusay.user.api.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserRestController のテスト")
class UserRestControllerTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    @WithMockUser
    @DisplayName("存在するユーザーIDでユーザー情報を取得できること")
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('750e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
            ON CONFLICT DO NOTHING;
            """
    })
    void testGetUser_Success() throws Exception {
        String userId = "750e8400-e29b-41d4-a716-446655440001";
        
        var assertResult = assertThat(mockMvcTester.get().uri("/users/{id}", userId))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$.id").asString().isEqualTo(userId);
        assertResult.bodyJson().extractingPath("$.username").asString().isEqualTo("admin");
        assertResult.bodyJson().extractingPath("$.email").asString().isEqualTo("admin@example.com");
        assertResult.bodyJson().extractingPath("$.enabled").asBoolean().isTrue();
        
        // パスワードハッシュは@JsonIgnoreで除外されているため、レスポンスに含まれないことを確認
        assertResult.bodyText().doesNotContain("passwordHash");
    }

    @Test
    @WithMockUser
    @DisplayName("存在しないユーザーIDで404エラーが返されること")
    void testGetUser_NotFound() throws Exception {
        String nonExistingUserId = "00000000-0000-0000-0000-000000000000";
        
        var assertResult = assertThat(mockMvcTester.get().uri("/users/{id}", nonExistingUserId))
                .hasStatus(404)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("User not found");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(404);
        assertResult.bodyJson().extractingPath("$.detail").asString().isEqualTo("User not found: " + nonExistingUserId);
    }

    @Test
    @DisplayName("認証なしでもアクセスできること（現在の設定ではpermitAllのため）")
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('750e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
            ON CONFLICT DO NOTHING;
            """
    })
    void testGetUser_WithoutAuth() throws Exception {
        String userId = "750e8400-e29b-41d4-a716-446655440001";
        
        assertThat(mockMvcTester.get().uri("/users/{id}", userId))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$.id").asString().isEqualTo(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("全てのユーザー情報を取得できること")
    @Sql(statements = {
            """
            DELETE FROM users;
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES 
                ('850e8400-e29b-41d4-a716-446655440001', 'user1', 'user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
                ('850e8400-e29b-41d4-a716-446655440002', 'user2', 'user2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
                ('850e8400-e29b-41d4-a716-446655440003', 'user3', 'user3@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false);
            """
    })
    void testGetAllUsers_Success() throws Exception {
        var assertResult = assertThat(mockMvcTester.get().uri("/users"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$").asList().hasSize(3);
        
        // 最初のユーザーが取得できていることを確認
        assertResult.bodyJson().extractingPath("$[?(@.username=='user1')].id").asList()
                .singleElement().isEqualTo("850e8400-e29b-41d4-a716-446655440001");
        assertResult.bodyJson().extractingPath("$[?(@.username=='user1')].email").asList()
                .singleElement().isEqualTo("user1@example.com");
        
        // パスワードハッシュは@JsonIgnoreで除外されているため、レスポンスに含まれないことを確認
        assertResult.bodyText().doesNotContain("passwordHash");
    }

    @Test
    @WithMockUser
    @DisplayName("ユーザーが存在しない場合は空のリストが返されること")
    @Sql(statements = "DELETE FROM users;")
    void testGetAllUsers_EmptyList() throws Exception {
        var assertResult = assertThat(mockMvcTester.get().uri("/users"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$").asList().isEmpty();
    }

    @Test
    @WithMockUser
    @DisplayName("存在するユーザーを削除できること")
    @Sql(statements = {
            """
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('950e8400-e29b-41d4-a716-446655440001', 'testuser', 'testuser@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
            ON CONFLICT DO NOTHING;
            """
    })
    void testDeleteUser_Success() throws Exception {
        String userId = "950e8400-e29b-41d4-a716-446655440001";
        
        assertThat(mockMvcTester.delete().uri("/users/{id}", userId))
                .hasStatus(204);
        
        // 削除後に取得しようとすると404エラーになることを確認
        assertThat(mockMvcTester.get().uri("/users/{id}", userId))
                .hasStatus(404);
    }

    @Test
    @WithMockUser
    @DisplayName("存在しないユーザーを削除しようとすると404エラーが返されること")
    void testDeleteUser_NotFound() throws Exception {
        String nonExistingUserId = "00000000-0000-0000-0000-000000000000";
        
        var assertResult = assertThat(mockMvcTester.delete().uri("/users/{id}", nonExistingUserId))
                .hasStatus(404)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("User not found");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(404);
        assertResult.bodyJson().extractingPath("$.detail").asString().isEqualTo("User not found: " + nonExistingUserId);
    }

    @Test
    @WithMockUser
    @DisplayName("存在するユーザーの情報を更新できること")
    @Sql(statements = {
            """
            DELETE FROM users WHERE id = 'a50e8400-e29b-41d4-a716-446655440001';
            INSERT INTO users (id, username, email, password_hash, enabled, account_non_expired, account_non_locked, credentials_non_expired)
            VALUES ('a50e8400-e29b-41d4-a716-446655440001', 'originaluser', 'original@example.com', '$2a$10$originalHash', true, true, true, true);
            """
    })
    void testUpdateUser_Success() throws Exception {
        String userId = "a50e8400-e29b-41d4-a716-446655440001";
        String requestBody = """
                {
                    "username": "updateduser",
                    "email": "updated@example.com",
                    "enabled": false
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.put().uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$.id").asString().isEqualTo(userId);
        assertResult.bodyJson().extractingPath("$.username").asString().isEqualTo("updateduser");
        assertResult.bodyJson().extractingPath("$.email").asString().isEqualTo("updated@example.com");
        assertResult.bodyJson().extractingPath("$.enabled").asBoolean().isFalse();
        
        // 更新されていない項目も確認
        assertResult.bodyJson().extractingPath("$.accountNonExpired").asBoolean().isTrue();
        assertResult.bodyJson().extractingPath("$.accountNonLocked").asBoolean().isTrue();
        assertResult.bodyJson().extractingPath("$.credentialsNonExpired").asBoolean().isTrue();
        
        // パスワードハッシュは@JsonIgnoreで除外されているため、レスポンスに含まれないことを確認
        assertResult.bodyText().doesNotContain("passwordHash");
    }

    @Test
    @WithMockUser
    @DisplayName("部分的な更新（一部のフィールドのみ更新）ができること")
    @Sql(statements = {
            """
            DELETE FROM users WHERE id = 'b50e8400-e29b-41d4-a716-446655440001';
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('b50e8400-e29b-41d4-a716-446655440001', 'partialuser', 'partial@example.com', '$2a$10$partialHash', true);
            """
    })
    void testUpdateUser_PartialUpdate() throws Exception {
        String userId = "b50e8400-e29b-41d4-a716-446655440001";
        String requestBody = """
                {
                    "email": "newpartial@example.com"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.put().uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$.id").asString().isEqualTo(userId);
        assertResult.bodyJson().extractingPath("$.username").asString().isEqualTo("partialuser");  // 変更されていない
        assertResult.bodyJson().extractingPath("$.email").asString().isEqualTo("newpartial@example.com");  // 変更された
        assertResult.bodyJson().extractingPath("$.enabled").asBoolean().isTrue();  // 変更されていない
    }

    @Test
    @WithMockUser
    @DisplayName("存在しないユーザーを更新しようとすると404エラーが返されること")
    void testUpdateUser_NotFound() throws Exception {
        String nonExistingUserId = "00000000-0000-0000-0000-000000000000";
        String requestBody = """
                {
                    "username": "newusername",
                    "email": "newemail@example.com"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.put().uri("/users/{id}", nonExistingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(404)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("User not found");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(404);
        assertResult.bodyJson().extractingPath("$.detail").asString().isEqualTo("User not found: " + nonExistingUserId);
    }

    @Test
    @WithMockUser
    @DisplayName("他のユーザーと重複するメールアドレスで更新しようとすると409エラーが返されること")
    @Sql(statements = {
            """
            DELETE FROM users WHERE id IN ('c50e8400-e29b-41d4-a716-446655440001', 'c50e8400-e29b-41d4-a716-446655440002');
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES 
                ('c50e8400-e29b-41d4-a716-446655440001', 'user1', 'user1@example.com', '$2a$10$hash1', true),
                ('c50e8400-e29b-41d4-a716-446655440002', 'user2', 'user2@example.com', '$2a$10$hash2', true);
            """
    })
    void testUpdateUser_DuplicateEmail() throws Exception {
        String userId = "c50e8400-e29b-41d4-a716-446655440001";
        String duplicateEmail = "user2@example.com";  // user2が既に使用しているメールアドレス
        String requestBody = String.format("""
                {
                    "email": "%s"
                }
                """, duplicateEmail);
        
        var assertResult = assertThat(mockMvcTester.put().uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(409)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Duplicate user");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(409);
        assertResult.bodyJson().extractingPath("$.detail").asString().isEqualTo("ユーザーが既に存在します: " + duplicateEmail);
    }

    @Test
    @WithMockUser
    @DisplayName("他のユーザーと重複するユーザー名で更新しようとすると409エラーが返されること")
    @Sql(statements = {
            """
            DELETE FROM users WHERE id IN ('d50e8400-e29b-41d4-a716-446655440001', 'd50e8400-e29b-41d4-a716-446655440002');
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES 
                ('d50e8400-e29b-41d4-a716-446655440001', 'user1', 'user1@example.com', '$2a$10$hash1', true),
                ('d50e8400-e29b-41d4-a716-446655440002', 'user2', 'user2@example.com', '$2a$10$hash2', true);
            """
    })
    void testUpdateUser_DuplicateUsername() throws Exception {
        String userId = "d50e8400-e29b-41d4-a716-446655440001";
        String duplicateUsername = "user2";  // user2が既に使用しているユーザー名
        String requestBody = String.format("""
                {
                    "username": "%s"
                }
                """, duplicateUsername);
        
        var assertResult = assertThat(mockMvcTester.put().uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(409)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Duplicate user");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(409);
        assertResult.bodyJson().extractingPath("$.detail").asString().isEqualTo("ユーザーが既に存在します: " + duplicateUsername);
    }

    @Test
    @WithMockUser
    @DisplayName("無効なメールアドレス形式で更新しようとすると400エラーが返されること")
    @Sql(statements = {
            """
            DELETE FROM users WHERE id = 'e50e8400-e29b-41d4-a716-446655440001';
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('e50e8400-e29b-41d4-a716-446655440001', 'validuser', 'valid@example.com', '$2a$10$hash', true);
            """
    })
    void testUpdateUser_InvalidEmail() throws Exception {
        String userId = "e50e8400-e29b-41d4-a716-446655440001";
        String requestBody = """
                {
                    "email": "invalid-email-format"
                }
                """;
        
        assertThat(mockMvcTester.put().uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(400);
    }

    @Test
    @WithMockUser
    @DisplayName("ユーザー名が長すぎる場合に400エラーが返されること")
    @Sql(statements = {
            """
            DELETE FROM users WHERE id = 'f50e8400-e29b-41d4-a716-446655440001';
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('f50e8400-e29b-41d4-a716-446655440001', 'validuser', 'valid@example.com', '$2a$10$hash', true);
            """
    })
    void testUpdateUser_UsernameTooLong() throws Exception {
        String userId = "f50e8400-e29b-41d4-a716-446655440001";
        String tooLongUsername = "a".repeat(51);  // 51文字（上限は50文字）
        String requestBody = String.format("""
                {
                    "username": "%s"
                }
                """, tooLongUsername);
        
        assertThat(mockMvcTester.put().uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(400);
    }
}
