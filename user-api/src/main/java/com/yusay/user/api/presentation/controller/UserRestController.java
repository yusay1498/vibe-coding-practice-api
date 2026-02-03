package com.yusay.user.api.presentation.controller;

import com.yusay.user.api.application.dto.DeleteAllResult;
import com.yusay.user.api.application.service.UserService;
import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.presentation.dto.DeleteAllRequest;
import com.yusay.user.api.presentation.dto.DeleteAllResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserRestController {
    
    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.list();
        return ResponseEntity.ok(users);
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

    /**
     * 全ユーザーを削除する（確認メカニズム付き）
     * 
     * 安全性のため、POSTメソッドを使用し、確認文字列を要求する。
     * DELETEメソッドではなくPOSTを使用するのは、誤操作を防ぐため。
     * 
     * @param request 削除リクエスト（確認文字列を含む）
     * @return 削除結果
     */
    @PostMapping("/delete-all")
    public ResponseEntity<DeleteAllResponse> deleteAllUsers(@RequestBody DeleteAllRequest request) {
        // 確認文字列のチェック
        if (!request.isConfirmed()) {
            return ResponseEntity.badRequest().body(
                new DeleteAllResponse(
                    false,
                    0,
                    null,
                    null,
                    String.format("確認文字列が正しくありません。'%s'を指定してください。", 
                        DeleteAllRequest.REQUIRED_CONFIRMATION)
                )
            );
        }
        
        // 全件削除を実行
        DeleteAllResult result = userService.deleteAll();
        
        DeleteAllResponse response = new DeleteAllResponse(
            true,
            result.deletedCount(),
            result.executedAt(),
            result.environment(),
            String.format("%d件のユーザーを削除しました。", result.deletedCount())
        );
        
        return ResponseEntity.ok(response);
    }
}
