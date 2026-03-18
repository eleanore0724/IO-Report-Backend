package com.example.demo.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class RsaKeyConfig {
	
	private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    
    @PostConstruct
    public void init() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");  // 取得 RSA 演算法的密鑰生成器
        keyPairGenerator.initialize(2048);  // 設定密鑰長度為 2048 位元
        KeyPair keyPair = keyPairGenerator.generateKeyPair();  // 實際產生出一組密鑰對 (包含公鑰和私鑰)
        
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        if(publicKey != null && privateKey != null) {
        	System.out.println("RSA 密鑰對產生成功！");
        }
    }

	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
	}
    
	
}
