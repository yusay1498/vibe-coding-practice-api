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
    @DisplayName("正しいデータで新規ユーザーを作成できること")
    @Sql(statements = "DELETE FROM users;")
    void testCreateUser_Success() throws Exception {
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "newuser@example.com",
                    "password": "password123"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(201)
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$.username").asString().isEqualTo("newuser");
        assertResult.bodyJson().extractingPath("$.email").asString().isEqualTo("newuser@example.com");
        assertResult.bodyJson().extractingPath("$.enabled").asBoolean().isTrue();
        
        // IDが生成されていることを確認
        assertResult.bodyJson().extractingPath("$.id").asString().isNotBlank();
        
        // パスワードハッシュは@JsonIgnoreで除外されているため、レスポンスに含まれないことを確認
        assertResult.bodyText().doesNotContain("passwordHash");
        assertResult.bodyText().doesNotContain("password123");
    }

    @Test
    @WithMockUser
    @DisplayName("重複するメールアドレスで新規ユーザーを作成しようとすると409エラーが返されること")
    @Sql(statements = {
            """
            DELETE FROM users;
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('850e8400-e29b-41d4-a716-446655440001', 'existinguser', 'existing@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true);
            """
    })
    void testCreateUser_DuplicateEmail() throws Exception {
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "existing@example.com",
                    "password": "password123"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(409)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Duplicate user");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(409);
    }

    @Test
    @WithMockUser
    @DisplayName("重複するユーザー名で新規ユーザーを作成しようとすると409エラーが返されること")
    @Sql(statements = {
            """
            DELETE FROM users;
            INSERT INTO users (id, username, email, password_hash, enabled)
            VALUES ('850e8400-e29b-41d4-a716-446655440001', 'existinguser', 'existing@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true);
            """
    })
    void testCreateUser_DuplicateUsername() throws Exception {
        String requestBody = """
                {
                    "username": "existinguser",
                    "email": "newemail@example.com",
                    "password": "password123"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(409)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Duplicate user");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(409);
    }

    @Test
    @WithMockUser
    @DisplayName("必須項目が欠けている場合は400エラーが返されること")
    void testCreateUser_MissingRequiredFields() throws Exception {
        String requestBody = """
                {
                    "username": "newuser"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(400)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Validation error");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(400);
    }

    @Test
    @WithMockUser
    @DisplayName("無効なメールアドレス形式の場合は400エラーが返されること")
    void testCreateUser_InvalidEmailFormat() throws Exception {
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(400)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Validation error");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(400);
    }

    @Test
    @WithMockUser
    @DisplayName("パスワードが短すぎる場合は400エラーが返されること")
    void testCreateUser_PasswordTooShort() throws Exception {
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "newuser@example.com",
                    "password": "short"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(400)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Validation error");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(400);
    }

    @Test
    @WithMockUser
    @DisplayName("ユーザー名が短すぎる場合は400エラーが返されること")
    void testCreateUser_UsernameTooShort() throws Exception {
        String requestBody = """
                {
                    "username": "ab",
                    "email": "newuser@example.com",
                    "password": "password123"
                }
                """;
        
        var assertResult = assertThat(mockMvcTester.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(400)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("Validation error");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(400);
    }
}
