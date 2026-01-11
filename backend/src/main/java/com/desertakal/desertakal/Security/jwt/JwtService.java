package com.desertakal.desertakal.Security.jwt;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access.expiration}")
    private Long accessExpiration;

    @Value("${security.jwt.refresh.expiration}")
    private Long refreshExpiration;

    private SecretKey getSecretKey() {return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));}

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUuid().toString())
                .claim("uuid", user.getUuid())
                .claim("role", user.getRole().getName())
                .claim("type", "ACCESS_TOKEN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUuid().toString())
                .claim("type", "REFRESH_TOKEN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSub(String token) {return extractClaim(token, Claims::getSubject);}

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValidToken(String token, CustomUserDetails userDetails) {
        try {
            final String uuid = extractSub(token);
            String tokenType = extractClaim(token, claims -> claims.get("type", String.class));

            return (userDetails.getUuid().equals(UUID.fromString(uuid))
                    && !isTokenExpired(token)
                    && "ACCESS_TOKEN".equals(tokenType));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
