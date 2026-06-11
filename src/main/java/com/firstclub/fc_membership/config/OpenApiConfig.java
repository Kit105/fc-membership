package com.firstclub.fc_membership.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI membershipOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FirstClub Membership API")
                        .description("""
                                Backend for FirstClub's tiered membership program.
                                
                                Quick start:
                                1. POST /api/v1/users — create a user
                                2. GET  /api/v1/plans — browse plans
                                3. GET  /api/v1/tiers — browse tiers and benefits
                                4. POST /api/v1/users/{userId}/membership/subscribe
                                5. POST /api/v1/orders — place orders (auto tier evaluation)
                                6. GET  /api/v1/users/{userId}/membership — check status
                                """)
                        .version("v1.0")
                        .contact(new Contact().name("FirstClub Engineering")));
    }
}