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
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserRestController のテスト")
class UserRestControllerTest {

    private static final String EXISTING_USER_ID = "750e8400-e29b-41d4-a716-446655440001";
    private static final String NON_EXISTING_USER_ID = "00000000-0000-0000-0000-000000000000";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    @WithMockUser
    @DisplayName("存在するユーザーIDでユーザー情報を取得できること")
    void testGetUser_Success() throws Exception {
        var assertResult = assertThat(mockMvcTester.get().uri("/users/{id}", EXISTING_USER_ID))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        assertResult.bodyJson().extractingPath("$.id").asString().isEqualTo(EXISTING_USER_ID);
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
        var assertResult = assertThat(mockMvcTester.get().uri("/users/{id}", NON_EXISTING_USER_ID))
                .hasStatus(404)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        assertResult.bodyJson().extractingPath("$.title").asString().isEqualTo("User not found");
        assertResult.bodyJson().extractingPath("$.status").asNumber().isEqualTo(404);
        assertResult.bodyJson().extractingPath("$.detail").asString().isEqualTo("User not found: " + NON_EXISTING_USER_ID);
    }

    @Test
    @DisplayName("認証なしでもアクセスできること（現在の設定ではpermitAllのため）")
    void testGetUser_WithoutAuth() throws Exception {
        assertThat(mockMvcTester.get().uri("/users/{id}", EXISTING_USER_ID))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$.id").asString().isEqualTo(EXISTING_USER_ID);
    }
}
