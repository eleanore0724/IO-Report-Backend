package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.example.demo.model.UserEntity;

@Mapper
public interface UserMapper {
	@Select("SELECT * FROM users WHERE username = #{username}")
    UserEntity findByUsername(String username);
	
	// 新增註冊用的 SQL
    @Insert("INSERT INTO users (username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "userId") // 讓 MyBatis 自動回填自增 ID
    int register(UserEntity user);
}
