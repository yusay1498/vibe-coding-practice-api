package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);
    int deleteById(String id);
    int deleteAll();
}
