package com.wr.nutmeg;

import com.wr.nutmeg.config.NutmegSeedProperties;
import com.wr.nutmeg.auth.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({NutmegSeedProperties.class, JwtProperties.class})
public class NutmegApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutmegApplication.class, args);
    }

}
