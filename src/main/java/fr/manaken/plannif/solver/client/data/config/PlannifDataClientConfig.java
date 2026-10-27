package fr.manaken.plannif.solver.client.data.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PlannifDataClientConfig {

    @Value("${plannif-data.api.base-url:http://localhost:${server.port:8080}}")
    private String baseUrl;

    @Bean
    public RestClient plannifDataRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}

