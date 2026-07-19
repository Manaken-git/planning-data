package fr.manaken.plannif.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plannif Data API")
                        .version("1.0.0")
                        .description("API REST pour la gestion et la planification des données scolaires (Professeurs, Élèves, Classes, Matières, Salles, Séances).")
                        .contact(new Contact()
                                .name("Équipe Manaken")
                                .email("contact@manaken.fr"))
                        .license(new License().name("Apache 2.0")));
    }
}
