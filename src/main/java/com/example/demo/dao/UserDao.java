package com.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.UserEntity;

public interface UserDao extends JpaRepository<UserEntity, Integer>{
    Optional<UserEntity> findByUsername(String username);
    
}
