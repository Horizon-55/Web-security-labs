package org.example.theminiprojectapijava;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // GET доступний всім авторизованим ролям
                        .requestMatchers(HttpMethod.GET, "/api/items/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        // POST і PUT доступні тільки для ADMIN та MANAGER
                        .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("ADMIN", "MANAGER")
                        // DELETE доступний ВИКЛЮЧНО для ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasRole("ADMIN")
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