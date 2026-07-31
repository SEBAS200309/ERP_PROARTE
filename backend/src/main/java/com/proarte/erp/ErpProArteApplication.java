package com.proarte.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ErpProArteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpProArteApplication.class, args);
    }
}
