package com.wr.nutmeg;

import com.wr.nutmeg.config.NutmegSeedProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NutmegSeedProperties.class)
public class NutmegApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutmegApplication.class, args);
    }

}
