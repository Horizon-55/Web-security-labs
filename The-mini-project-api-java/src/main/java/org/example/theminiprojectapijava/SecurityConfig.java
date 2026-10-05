package org.example.theminiprojectapijava;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Вимикаємо CSRF для REST API
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // Всі запити вимагають авторизації
                )
                .httpBasic(Customizer.withDefaults()); // Вмикаємо Basic Auth

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Створюємо користувача в пам'яті для тестування (логін: admin, пароль: password)
        UserDetails user = User.builder()
                .username("admin")
                .password("{noop}password") // {noop} означає, що пароль не хешується для простоти
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}