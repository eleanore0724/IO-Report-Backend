package com.example.demo.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.dao.UserMapper;
import com.example.demo.model.UserEntity;

@Service
public class UserService implements UserDetailsService {

	@Autowired
    private UserMapper userMapper;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	/*
	 * InMemory 改為 MyBatis + 資料庫 連動，核心在於實作 UserDetailsService 介面。
	 * Spring Security 的 AuthenticationManager 在執行 authenticate 時，
	 * 會自動呼叫這個介面來從資料庫抓取使用者資訊進行比對。
	 */
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
        UserEntity user = userMapper.findByUsername(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("找不到使用者: " + username);
        }

        // 將資料庫查到的資料包裝成 Spring Security 的 User 物件
		return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole()) // 這裡會自動加上 "ROLE_" 前綴
                .build();
	}
	

	public String registerUser(UserEntity user) {
		if (userMapper.findByUsername(user.getUsername()) != null) {
            return "帳號已存在";
        }
		
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        if (user.getRole() == null) user.setRole("USER");
		
        userMapper.register(user);
        return "註冊成功";
	}
	
}
