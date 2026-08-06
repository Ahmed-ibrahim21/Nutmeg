package com.wr.nutmeg.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "nutmeg.jwt")
public class JwtProperties {

    private String secret = "change-me-use-at-least-32-characters-long-secret-key";
    private long expirationMs = 86_400_000L;
}
