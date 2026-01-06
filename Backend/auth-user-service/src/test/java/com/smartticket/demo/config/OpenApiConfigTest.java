package com.smartticket.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void customOpenAPI_returnsConfiguredOpenAPI() {
      
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Smart Ticket User Auth Service API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertTrue(info.getDescription().contains("user registration"));
    }

    @Test
    void customOpenAPIBeanAvailableInContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(OpenApiConfig.class)) {
            OpenAPI openAPI = context.getBean(OpenAPI.class);
            assertNotNull(openAPI);
            assertEquals("Smart Ticket User Auth Service API", openAPI.getInfo().getTitle());
        }
    }
}
