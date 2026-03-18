package com.example.demo.dao;

import java.util.Optional;


import com.example.demo.model.UserEntity;

public interface UserDao {
    Optional<UserEntity> findByUsername(String username);
    
}
