package com.example.demo.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.demo.config.RsaKeyConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
public class JwtUtil {
	
	@Autowired
	private RsaKeyConfig rsaKeyConfig;
	
	private final long EXPIRATION_TIME = 1000 * 60 * 60;
	
	// 產生 JWT Token (使用私鑰簽名)
	public String generateToken(String username, String role) {
		return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // 將使用者權限(Role)放進 Token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(rsaKeyConfig.getPrivateKey(), SignatureAlgorithm.RS256) // 使用 RSA 私鑰加密
                .compact();
	}
	
	
	// 解析並驗證 JWT Token (使用公鑰解密)
    public Claims validateTokenAndGetClaims(String token) {
    	try {
            return Jwts.parserBuilder()
                    .setSigningKey(rsaKeyConfig.getPublicKey()) // 使用 RSA 公鑰驗證
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Token 驗證失敗: " + e.getMessage());
        }
    }
}
