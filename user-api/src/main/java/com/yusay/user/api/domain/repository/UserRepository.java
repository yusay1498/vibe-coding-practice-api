package com.yusay.user.api.domain.repository;

import com.yusay.user.api.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
}
