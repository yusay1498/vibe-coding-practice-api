package com.yusay.user.api.presentation.controller;

import com.yusay.user.api.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserRestController のテスト")
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("存在するユーザーIDでユーザー情報を取得できること")
    void testGetUser_Success() throws Exception {
        // テストデータから存在するユーザーID
        String existingUserId = "750e8400-e29b-41d4-a716-446655440001";

        mockMvc.perform(get("/users/{id}", existingUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(existingUserId))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()); // パスワードハッシュは返されないこと
    }

    @Test
    @WithMockUser
    @DisplayName("存在しないユーザーIDで404エラーが返されること")
    void testGetUser_NotFound() throws Exception {
        // 存在しないユーザーID
        String nonExistingUserId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/users/{id}", nonExistingUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("User not found: " + nonExistingUserId));
    }

    @Test
    @DisplayName("認証なしでアクセスすると401エラーが返されること")
    void testGetUser_Unauthorized() throws Exception {
        String existingUserId = "750e8400-e29b-41d4-a716-446655440001";

        mockMvc.perform(get("/users/{id}", existingUserId))
                .andExpect(status().isUnauthorized());
    }
}
