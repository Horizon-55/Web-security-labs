package org.example.theminiprojectapijava.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.theminiprojectapijava.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — перевіряє заголовок Authorization у кожному запиті.
 *
 * Як це працює:
 * 1. Перевіряємо, чи є заголовок "Authorization"
 * 2. Якщо так, перевіряємо, чи починається він з "Bearer "
 * 3. Якщо так, витягуємо token
 * 4. Витягуємо username з токена (JwtService.extractUsername)
 * 5. Перевіряємо, чи токен валідний (JwtService.isValid)
 * 6. Якщо так, завантажуємо UserDetails та встановлюємо їх у SecurityContextHolder
 *
 * Це дозволяє Spring Security знати, хто зараз авторизований, для кожної
 * анотації @PreAuthorize("hasRole('ADMIN')") на контролерах.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Витягуємо заголовок Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Перевіряємо, чи є заголовок і чи починається з "Bearer "
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // Якщо токена немає — пропускаємо запит далі (можливо, це логін або публічний ендпоінт)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Витягуємо токен без префіксу "Bearer "
        final String token = authHeader.substring(7);

        try {
            // 4. Витягуємо username з токена
            final String username = jwtService.extractUsername(token);

            // 5. Перевіряємо, чи токен валідний і чи користувач ще не авторизований
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. Завантажуємо UserDetails (з ролями)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 7. Перевіряємо валідність токена
                if (jwtService.isValid(token, userDetails)) {
                    // 8. Створюємо Authentication токен
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 9. Додаємо деталі запиту
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 10. Встановлюємо Authentication в SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // У разі невалідного або простроченого токена — просто продовжуємо ланцюг без авторизації
            // Spring Security далі поверне 401/403 згідно з правилами
        }

        // 11. Передаємо запит далі по ланцюжку
        filterChain.doFilter(request, response);
    }
}