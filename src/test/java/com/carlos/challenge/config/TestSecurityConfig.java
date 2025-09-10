package com.carlos.challenge.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
@EnableMethodSecurity
@Import(com.carlos.challenge.infrastructure.config.SecurityConfig.class)
public class TestSecurityConfig {

    @Bean
    UserDetailsService testUsers() {
        PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new InMemoryUserDetailsManager(
            User.withUsername("user").password(pe.encode("pass")).roles("USER").build(),
            User.withUsername("admin").password(pe.encode("pass")).roles("ADMIN").build()
        );
    }
}
