package com.yusay.user.api.presentation.controller;

import com.yusay.user.api.application.dto.DeleteAllResult;
import com.yusay.user.api.application.service.UserService;
import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import com.yusay.user.api.presentation.dto.CreateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserRestController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserRestController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.list();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        // パスワードをハッシュ化
        String passwordHash = passwordEncoder.encode(request.password());
        
        // ユーザーを作成
        User createdUser = userService.create(
                request.username(),
                request.email(),
                passwordHash
        );
        
        // 作成されたリソースのURIを構築
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();
        
        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userService.lookup(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<DeleteAllResult> deleteAllUsers(
            @RequestHeader(value = "X-Confirm-Delete-All", required = false) String confirmHeader) {
        // 破壊的操作のため、確認ヘッダーを要求
        if (confirmHeader == null || !confirmHeader.equals("true")) {
            throw new DeleteAllNotAllowedException(
                "全件削除には X-Confirm-Delete-All: true ヘッダーが必要です");
        }
        
        DeleteAllResult result = userService.deleteAll();
        return ResponseEntity.ok(result);
    }
}
