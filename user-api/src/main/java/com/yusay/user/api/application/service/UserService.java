package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.DuplicateUserException;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import com.yusay.user.api.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    public UserService(UserRepository userRepository, UserDomainService userDomainService) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
    }

    /**
     * 新規ユーザーを作成する
     * 
     * @param username ユーザー名
     * @param email メールアドレス
     * @param passwordHash パスワードハッシュ
     * @return 作成されたユーザー
     * @throws DuplicateUserException メールアドレスまたはユーザー名が既に存在する場合
     */
    public User create(String username, String email, String passwordHash) {
        // メールアドレスの重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException("Email already exists: " + email);
        }
        
        // ユーザー名の重複チェック
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUserException("Username already exists: " + username);
        }
        
        // ドメインサービスを使用してユーザーを作成
        User newUser = userDomainService.createUser(
                null,  // IDはリポジトリ層で生成される
                username,
                email,
                passwordHash,
                true,  // enabled
                true,  // accountNonExpired
                true,  // accountNonLocked
                true   // credentialsNonExpired
        );
        
        // リポジトリに保存
        return userRepository.save(newUser);
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
}
