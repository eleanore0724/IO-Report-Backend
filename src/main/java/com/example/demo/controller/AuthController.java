package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.UserEntity;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
    private JwtUtil jwtUtil;
	
	@Autowired
    private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // username, password, roles

            // 抓取第一個權限並把 "ROLE_" 前綴拿掉，方便存入 JWT
            String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

            // 使用 RSA 產生 JWT Token
            String token = jwtUtil.generateToken(username, role);

            // 回傳 Token 給前端
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("帳號或密碼錯誤");
        }
    }
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserEntity user) {
	    String result = userService.registerUser(user);
	    if ("註冊成功".equals(result)) {
	        return ResponseEntity.ok(result);
	    }
	    return ResponseEntity.badRequest().body(result);
	}
}
