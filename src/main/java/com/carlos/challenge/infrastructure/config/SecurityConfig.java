package com.carlos.challenge.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String API_ACCREDITATIONS = "/api/accreditations/**";
    public static final String API_ACCREDITATIONS1 = "/api/accreditations";
    private final SecurityUsersProperties securityUsers;

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        if (securityUsers.getUsers() != null) {
            for (SecurityUsersProperties.User u : securityUsers.getUsers()) {
                String raw = u.getPassword() != null ? u.getPassword() : "";
                String pwd = raw.startsWith("{") ? raw : encoder.encode(raw);
                String[] roles = u.getRoles() != null ? u.getRoles().split("\\s*,\\s*") : new String[0];
                manager.createUser(User.withUsername(u.getUsername()).password(pwd).roles(roles).build());
            }
        }
        return manager;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    private JwtAuthenticationConverter jwtAuthConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String scope = (String) jwt.getClaims().getOrDefault("scope", "");
            return java.util.Arrays.stream(scope.split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                    .map(s -> (GrantedAuthority) new org.springframework.security.core.authority.SimpleGrantedAuthority(s))
                    .toList();
        });
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/auth/token").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                API_ACCREDITATIONS1,
                                API_ACCREDITATIONS
                        ).hasRole(ADMIN)

                        .requestMatchers(HttpMethod.PUT,
                                API_ACCREDITATIONS1,
                                API_ACCREDITATIONS
                        ).hasRole(ADMIN)

                        .requestMatchers(HttpMethod.DELETE,
                                API_ACCREDITATIONS1,
                                API_ACCREDITATIONS
                        ).hasRole(ADMIN)

                        .requestMatchers(HttpMethod.GET,
                                API_ACCREDITATIONS1,
                                API_ACCREDITATIONS
                        ).hasAnyRole(USER, ADMIN)

                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                )
                ;

        return http.build();
    }

}
