package com.yusay.user.api.controller;

import com.yusay.user.api.TestcontainersConfiguration;
import com.yusay.user.api.entity.User;
import com.yusay.user.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserService userService;

    @Test
    void findById_存在しないユーザーの場合空のOptionalを返す() {
        Optional<User> result = userService.findById("non-existent-id");
        assertThat(result).isEmpty();
    }
}
