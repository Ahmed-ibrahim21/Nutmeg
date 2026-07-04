package com.wr.nutmeg.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "nutmeg.seed")
public class NutmegSeedProperties {

    private boolean enabled = false;
    private String leagueName = "Division 3";
    private String country = "England";
    private int tier = 3;
    private int clubCount = 8;
}
