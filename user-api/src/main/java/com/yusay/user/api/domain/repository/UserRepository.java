package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    List<User> findAll();
}
