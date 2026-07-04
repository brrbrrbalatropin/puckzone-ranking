package com.puckzone.ranking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PuckzoneRankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PuckzoneRankingApplication.class, args);
    }

}
