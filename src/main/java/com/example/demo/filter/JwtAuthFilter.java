package com.example.demo.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 每一個發送到 /api/... 的請求，都會先經過這個 Filter。它會去檢查 HTTP Header 裡面有沒有攜帶 Token。

@Component
public class JwtAuthFilter extends OncePerRequestFilter{ // 每一次 HTTP 請求，都只會經過這個過濾器一次

	@Autowired
	public JwtUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        	String token = authHeader.substring(7);
        	try {
        		// 驗證 Token 並取得裡面的資料 (Claims)
                Claims claims = jwtUtil.validateTokenAndGetClaims(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class); // 請把拿出來的 role 當作字串 (String) 來看待
                System.out.println("role:" + role);

                // 告訴 Spring Security 這個用戶是合法的，並且賦予對應權限
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role); // 字串包裝起來，變成警衛看得懂的「識別證」
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority)); // 製作身分證(你是誰, 使用者的密碼, 你的權限清單）
                
                SecurityContextHolder.getContext().setAuthentication(authentication); // 掛上身分證
        	} catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // Token 過期或被竄改，設定 401 Unauthorized
                response.getWriter().write("Invalid Token");
                return;
        	}
        }
        
        // 繼續往下走 (可能是去 Controller 或下一個 Filter)
        filterChain.doFilter(request, response);
        
	}

}
