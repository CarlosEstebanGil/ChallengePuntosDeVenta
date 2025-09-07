package com.carlos.challenge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityUsersProperties {
    private List<User> users;

    @Getter
    @Setter
    public static class User {
        private String username;
        private String password;
        private String roles;
    }
}