package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
}
