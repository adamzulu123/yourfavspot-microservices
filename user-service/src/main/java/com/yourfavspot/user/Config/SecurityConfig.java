package com.yourfavspot.user.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Jeśli nie masz formularzy, można zostawić wyłączone
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/user/registerDto").permitAll() // Endpointy publiczne
                        .anyRequest().authenticated() // Wszystkie inne endpointy wymagają autoryzacji
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // Uwierzytelnianie JWT
                .formLogin(Customizer.withDefaults()); // Logowanie przez formularz

        return http.build();
    }
}
