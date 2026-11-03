package org.example.theminiprojectapijava.controller;

import org.example.theminiprojectapijava.JwtService;
import org.example.theminiprojectapijava.request.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Контролер автентифікації — Лабораторна №8 (JWT).
 *
 * POST /api/auth/login
 * {
 *   "username": "admin",
 *   "password": "admin123"
 * }
 *
 * Відповідь:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 *
 * Доступні користувачі (визначені в SecurityConfig):
 *   admin / admin123  -> ROLE_ADMIN
 *   manager / manager123 -> ROLE_MANAGER
 *   user / user123    -> ROLE_USER
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    /**
     * Логін — отримання JWT-токена
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Автентифікуємо користувача (перевірка username/password)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Завантажуємо UserDetails (з ролями)
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

            // 3. Генеруємо JWT-токен
            String token = jwtService.generateToken(userDetails);

            // 4. Повертаємо токен клієнту
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (BadCredentialsException e) {
            // Невірні облікові дані
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Невірне ім'я користувача або пароль"));
        } catch (Exception e) {
            // Інша помилка
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Помилка авторизації: " + e.getMessage()));
        }
    }

    /**
     * Відповідь на успішний логін
     */
    public record AuthResponse(String token) {
    }

    /**
     * Помилка авторизації
     */
    public record ErrorResponse(String message) {
    }
}
