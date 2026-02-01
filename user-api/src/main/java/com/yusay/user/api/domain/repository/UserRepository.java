package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(String id);
    User save(User user);
    int deleteById(String id);
}
