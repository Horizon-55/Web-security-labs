package org.example.theminiprojectapijava;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервіс для генерації та перевірки JWT-токенів.
 * Використовує бібліотеку jjwt 0.11.5 (алгоритм HS256).
 */
@Service
public class JwtService {

    /**
     * 🔑 Секретний ключ для підпису токенів.
     * Мінімальна довжина для HS256 — 256 біт (32 байти).
     * 🔒 У продакшені БЕРИ З ENVIRONMENT VARIABLE, НЕ ХАРКОДЬ!
     */
    @Value("${jwt.secret:mySecretKeyMySecretKeyMySecretKeyMySecretKey}")
    private String jwtSecret;

    /**
     * ⏳ Тривалість життя токена в хвилинах (за замовчуванням 1440 = 24 години)
     */
    @Value("${jwt.expiration:1440}")
    private long jwtExpirationMinutes;

    /**
     * Витягує конкретний claim з токена
     * @param token JWT-токен
     * @param claimsResolver функція для витягування значення claim'а
     * @param <T> тип поверненого значення
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Витягує ім'я користувача з claim 'sub' (subject)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Витягує дату закінчення дії токена (claim 'exp')
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Парсить і розшифровує всі claim'и з токена за допомогою секретного ключа
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Перевірка: закінчився термін дії токена?
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Генерує JWT-токен для автентифікованого користувача
     * Включає: subject (username), roles, issuedAt, expiration, підпис HS256
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Додаємо ролі у payload для наочності на jwt.io
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Створює підписаний токен з додатковими claim'ами
     */
    private String createToken(Map<String, Object> claims, String username) {
        long expiration = System.currentTimeMillis() + jwtExpirationMinutes * 60 * 1000;

        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Перевіряє валідність токена: правильний username + не закінчився термін
     */
    public Boolean isValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Формує коректний заголовок Authorization для запитів
     * Приклад: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     */
    public String prepareAuthorizationHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Конвертує секретний рядок у ключ HMAC-SHA256
     */
    private Key getSignInKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}