package com.example.kidsapp.config;

import com.example.kidsapp.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Allow the login page and the javascript bundle to load freely
                        .requestMatchers("/login", "/bundle.js", "/static/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/parent/**").hasRole("PARENT")
                        .requestMatchers("/api/kid/**").hasAnyRole("KID", "PARENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // 3. Let Spring Boot handle the default login page generation cleanly
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRole()) // Assumes roles are stored as "ROLE_KID" / "ROLE_PARENT"
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Plaintext fallback for kid-friendly simplicity. Use BCrypt for real production!
        return NoOpPasswordEncoder.getInstance();
    }
}