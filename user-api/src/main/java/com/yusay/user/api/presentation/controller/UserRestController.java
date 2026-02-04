package com.yusay.user.api.presentation.controller;

import com.yusay.user.api.application.dto.DeleteAllResult;
import com.yusay.user.api.application.service.UserService;
import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import com.yusay.user.api.presentation.constant.ErrorMessages;
import com.yusay.user.api.presentation.constant.HttpHeaders;
import com.yusay.user.api.presentation.dto.CreateUserRequest;
import com.yusay.user.api.presentation.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    
    private static final String CONFIRM_VALUE = "true";
    
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

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
        // パスワードが指定されている場合はハッシュ化
        String passwordHash = null;
        if (request.password() != null) {
            passwordHash = passwordEncoder.encode(request.password());
        }
        
        // ユーザーを更新
        User updatedUser = userService.update(
                id,
                request.username(),
                request.email(),
                passwordHash,
                request.enabled(),
                request.accountNonExpired(),
                request.accountNonLocked(),
                request.credentialsNonExpired()
        );
        
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<DeleteAllResult> deleteAllUsers(
            @RequestHeader(value = HttpHeaders.CONFIRM_DELETE_ALL, required = false) String confirmHeader) {
        // 破壊的操作のため、確認ヘッダーを要求
        if (confirmHeader == null || !CONFIRM_VALUE.equalsIgnoreCase(confirmHeader)) {
            throw new DeleteAllNotAllowedException(ErrorMessages.DELETE_ALL_NOT_ALLOWED);
        }
        
        DeleteAllResult result = userService.deleteAll();
        return ResponseEntity.ok(result);
    }
}
