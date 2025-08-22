package com.pollsystem.springapp.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.pollsystem.springapp.security.services.UserPrincipal;
import javax.crypto.SecretKey;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import javax.annotation.PostConstruct;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs;

    private SecretKey signingKey;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            this.signingKey = createSigningKey();
            logger.info("JWT signing key initialized successfully with {} bit key", 
                       getKeySize() * 8);
        } catch (Exception e) {
            logger.error("Failed to initialize JWT signing key: {}", e.getMessage());
            throw new RuntimeException("JWT configuration error. " + 
                "Please generate a secure JWT secret key. " + e.getMessage(), e);
        }
    }

    private SecretKey createSigningKey() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalArgumentException(
                "JWT secret is not configured. Please set 'app.jwt.secret' in application.properties. " +
                "Generate a secure key using: openssl rand -base64 64"
            );
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            
            // For HS256, minimum 32 bytes (256 bits)
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException(
                    String.format(
                        "JWT secret key is too short. Required: minimum 256 bits (32 bytes), " +
                        "Current: %d bits (%d bytes). " +
                        "Generate a secure key using: openssl rand -base64 64",
                        keyBytes.length * 8, keyBytes.length
                    )
                );
            }

            return Keys.hmacShaKeyFor(keyBytes);

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new IllegalArgumentException(
                    "JWT secret must be Base64 encoded. Generate a secure key using: openssl rand -base64 64"
                );
            }
            throw e;
        }
    }

    private int getKeySize() {
        if (signingKey != null) {
            return signingKey.getEncoded().length;
        }
        return 0;
    }

    public String generateJwtToken(Authentication authentication) {
        if (signingKey == null) {
            throw new IllegalStateException("JWT signing key is not initialized");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        try {
            return Jwts.builder()
                    .setSubject(userPrincipal.getEmail())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .setIssuer("pollsystem-app")
                    .signWith(signingKey, SignatureAlgorithm.HS256) // Using HS256 for compatibility
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public String getEmailFromJwtToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("JWT token cannot be null or empty");
        }
        
        if (signingKey == null) {
            throw new IllegalStateException("JWT signing key is not initialized");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer("pollsystem-app")
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getSubject();
        } catch (Exception e) {
            logger.debug("Error extracting email from JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to extract email from JWT token", e);
        }
    }

    public boolean validateJwtToken(String authToken) {
        if (!StringUtils.hasText(authToken)) {
            return false;
        }

        if (signingKey == null) {
            logger.error("JWT signing key is not initialized");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer("pollsystem-app")
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.debug("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.debug("JWT token validation error: {}", e.getMessage());
        }

        return false;
    }

    // Additional utility methods
    public boolean isTokenExpired(String token) {
        if (signingKey == null) return true;

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}