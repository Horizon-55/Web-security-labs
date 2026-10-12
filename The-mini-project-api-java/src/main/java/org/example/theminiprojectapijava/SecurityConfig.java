package org.example.theminiprojectapijava;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Дозволяємо доступ до статичних ресурсів (якщо є фронтенд)
                        .requestMatchers("/", "/index.html", "/static/**").permitAll()
                        // Всі запити до /api/items/** вимагають автентифікації.
                        // Детальний контроль доступу за ролями (RBAC) здійснюється анотаціями @PreAuthorize на методах контролера.
                        .requestMatchers("/api/items/**").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Створюємо 3 користувачів з різними рівнями доступу
        UserDetails admin = User.builder()
                .username("admin").password("{noop}admin123").roles("ADMIN").build();

        UserDetails manager = User.builder()
                .username("manager").password("{noop}manager123").roles("MANAGER").build();

        UserDetails user = User.builder()
                .username("user").password("{noop}user123").roles("USER").build();

        return new InMemoryUserDetailsManager(admin, manager, user);
    }
}