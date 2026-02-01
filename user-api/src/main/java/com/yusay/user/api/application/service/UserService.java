package com.yusay.user.api.application.service;

import com.yusay.user.api.domain.entity.User;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
