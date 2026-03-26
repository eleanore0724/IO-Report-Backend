package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.demo.filter.JwtAuthFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
    private JwtAuthFilter jwtAuthFilter;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	/*
	@Bean
	public InMemoryUserDetailsManager inMemoryUserDetailsManager() {

		UserDetails accountA = User.withUsername("accountA")
				.password("{noop}123456")
				.roles("USER") // 會自動被 Spring 轉成 "ROLE_USER"
				.build();


		UserDetails accountB = User.withUsername("accountB")
				.password("{noop}123456")
				.roles("ADMIN") 
				.build();

		return new InMemoryUserDetailsManager(accountA, accountB);
	}
	*/

	// Spring Security 內建的驗證中心公開出來，讓 Controller 可以呼叫它來驗證帳密
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http
		.cors(Customizer.withDefaults()) 
        .csrf(csrf -> csrf.disable()) 
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 
        .authorizeHttpRequests(request -> request
        	// 放行所有 OPTIONS 預檢請求
            .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/auth/login", "/auth/register").permitAll()
            .requestMatchers("/api/dvds").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/generate").hasRole("ADMIN")
            .requestMatchers("/api/upload").hasRole("ADMIN")
            .anyRequest().authenticated()  // 其他沒有設定到的請求，一律需要登入
        )
        // 內建的帳號密碼檢查之前，先檢查請求頭（Header）是否有自定義的 Token。
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
	}
	
	
	
}
