package com.example.kidsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

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
                        // 2. Protect everything else (including "/" and our APIs)
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
    public UserDetailsService userDetailsService() {
        UserDetails kid = User.withDefaultPasswordEncoder()
                .username("kid")
                .password("stars")
                .roles("KID")
                .build();

        UserDetails parent = User.withDefaultPasswordEncoder()
                .username("parent")
                .password("superboss")
                .roles("PARENT")
                .build();

        return new InMemoryUserDetailsManager(kid, parent);
    }
}