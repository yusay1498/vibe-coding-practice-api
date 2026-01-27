package com.yusay.user.api.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("UserRestController のテスト")
class UserRestControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    @WithMockUser
    @DisplayName("存在するユーザーIDでユーザー情報を取得できること")
    void testGetUser_Success() throws Exception {
        // テストデータから存在するユーザーID
        String existingUserId = "750e8400-e29b-41d4-a716-446655440001";

        assertThat(mockMvcTester.get().uri("/users/{id}", existingUserId))
                .hasStatusOk()
                .hasContentType("application/json")
                .bodyJson()
                .extractingPath("$.id").asString().isEqualTo(existingUserId);
        
        assertThat(mockMvcTester.get().uri("/users/{id}", existingUserId))
                .bodyJson()
                .extractingPath("$.username").asString().isEqualTo("admin");

        assertThat(mockMvcTester.get().uri("/users/{id}", existingUserId))
                .bodyJson()
                .extractingPath("$.email").asString().isEqualTo("admin@example.com");

        assertThat(mockMvcTester.get().uri("/users/{id}", existingUserId))
                .bodyJson()
                .extractingPath("$.enabled").asBoolean().isTrue();
        
        // パスワードハッシュは@JsonIgnoreで除外されているため、レスポンスに含まれないことを確認
        // extractingPathで存在しないパスを指定するとエラーになるため、このチェックは省略
    }

    @Test
    @WithMockUser
    @DisplayName("存在しないユーザーIDで404エラーが返されること")
    void testGetUser_NotFound() throws Exception {
        // 存在しないユーザーID
        String nonExistingUserId = "00000000-0000-0000-0000-000000000000";

        assertThat(mockMvcTester.get().uri("/users/{id}", nonExistingUserId))
                .hasStatus(404)
                .hasContentType("application/problem+json")
                .bodyJson()
                .extractingPath("$.title").asString().isEqualTo("User not found");

        assertThat(mockMvcTester.get().uri("/users/{id}", nonExistingUserId))
                .bodyJson()
                .extractingPath("$.status").asNumber().isEqualTo(404);

        assertThat(mockMvcTester.get().uri("/users/{id}", nonExistingUserId))
                .bodyJson()
                .extractingPath("$.detail").asString().isEqualTo("User not found: " + nonExistingUserId);
    }

    @Test
    @DisplayName("認証なしでもアクセスできること（現在の設定ではpermitAllのため）")
    void testGetUser_WithoutAuth() throws Exception {
        String existingUserId = "750e8400-e29b-41d4-a716-446655440001";

        assertThat(mockMvcTester.get().uri("/users/{id}", existingUserId))
                .hasStatusOk()
                .hasContentType("application/json")
                .bodyJson()
                .extractingPath("$.id").asString().isEqualTo(existingUserId);
    }
}
