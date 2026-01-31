package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    User save(User user);
}
