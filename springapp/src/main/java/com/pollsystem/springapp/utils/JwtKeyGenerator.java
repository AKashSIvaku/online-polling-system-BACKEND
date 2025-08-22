package com.pollsystem.springapp.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        System.out.println("=== JWT SECRET KEY GENERATOR ===\n");
        
        // Generate secure keys
        String key256 = generateSecureKey(32); // 256-bit for HS256
        String key512 = generateSecureKey(64); // 512-bit for HS512
        
        // Using JJWT library
        String jjwtKey = Base64.getEncoder().encodeToString(
            Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
        );
        
        System.out.println("Choose one of these secure keys for your application.properties:\n");
        
        System.out.println("# Option 1: 256-bit key (minimum for HS256)");
        System.out.println("app.jwt.secret=" + key256);
        System.out.println();
        
        System.out.println("# Option 2: 512-bit key (more secure)");
        System.out.println("app.jwt.secret=" + key512);
        System.out.println();
        
        System.out.println("# Option 3: JJWT generated key");
        System.out.println("app.jwt.secret=" + jjwtKey);
        System.out.println();
        
        System.out.println("Copy one of the above lines to your application.properties file");
        System.out.println("Recommended: Use Option 2 (512-bit) for maximum security");
    }
    
    private static String generateSecureKey(int bytes) {
        byte[] keyBytes = new byte[bytes];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}