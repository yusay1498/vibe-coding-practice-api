package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import com.yusay.user.api.presentation.dto.CreateUserRequest;
import com.yusay.user.api.presentation.dto.UpdateUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User lookup(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    public void delete(String id) {
        int deletedCount = userRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new UserNotFoundException(id);
        }
    }

    public User create(CreateUserRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String hashedPassword = passwordEncoder.encode(request.password());
        
        User newUser = new User(
            null,  // IDはリポジトリ層で生成される
            request.username(),
            request.email(),
            hashedPassword,
            true,  // デフォルトで有効
            true,  // デフォルトで期限切れなし
            true,  // デフォルトでロックなし
            true,  // デフォルトで認証情報期限切れなし
            now,
            now
        );
        
        return userRepository.save(newUser);
    }

    public User update(String id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        LocalDateTime now = LocalDateTime.now();
        
        // 更新されたフィールドのみを反映（nullでないもの）
        String updatedUsername = request.username() != null ? request.username() : existingUser.username();
        String updatedEmail = request.email() != null ? request.email() : existingUser.email();
        String updatedPasswordHash = request.password() != null 
                ? passwordEncoder.encode(request.password()) 
                : existingUser.passwordHash();
        Boolean updatedEnabled = request.enabled() != null ? request.enabled() : existingUser.enabled();
        Boolean updatedAccountNonExpired = request.accountNonExpired() != null 
                ? request.accountNonExpired() 
                : existingUser.accountNonExpired();
        Boolean updatedAccountNonLocked = request.accountNonLocked() != null 
                ? request.accountNonLocked() 
                : existingUser.accountNonLocked();
        Boolean updatedCredentialsNonExpired = request.credentialsNonExpired() != null 
                ? request.credentialsNonExpired() 
                : existingUser.credentialsNonExpired();
        
        User updatedUser = new User(
            existingUser.id(),
            updatedUsername,
            updatedEmail,
            updatedPasswordHash,
            updatedEnabled,
            updatedAccountNonExpired,
            updatedAccountNonLocked,
            updatedCredentialsNonExpired,
            existingUser.createdAt(),  // 作成日時は保持
            now  // 更新日時を更新
        );
        
        return userRepository.save(updatedUser);
    }
}
