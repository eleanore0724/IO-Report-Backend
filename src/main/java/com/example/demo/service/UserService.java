package com.example.demo.service;

/*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import com.example.demo.dao.UserDao;
import com.example.demo.model.UserEntity;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	// 去 MySQL 找人
        UserEntity user = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到帳號: " + username));

        // 轉換成 Spring Security 認識的 User，並塞入密碼與權限
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // 資料庫密碼
                .roles(user.getRole())        // ex: USER / ADMIN
                .build();
    }
}
*/