package edu.utec.sienep.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SIENEP API")
                .description("""
                    **Sistema Integral de Estudiantes con Necesidades Educativas Personalizadas — UTEC**

                    API RESTful desarrollada con Spring Boot 3, Spring Security y JWT.

                    ## Autenticación
                    1. Registrar un funcionario en `POST /auth/registro`
                    2. Obtener el token JWT del response
                    3. Hacer clic en **Authorize** e ingresar el token
                    4. Todos los endpoints protegidos quedan habilitados

                    ## Módulos implementados
                    - **RF01–RF04** Autenticación, cierre de sesión y cambio de contraseña
                    - **RF05–RF09** Gestión completa de estudiantes e informes médicos
                    - **RF10–RF18** Instancias con categorías, clonación y notificación automática
                    - **RF19–RF27** Recordatorios recurrentes con conversión a instancias
                    - **RF28–RF29** Incidencias con historial
                    - **RF30–RF31** Reportes PDF exportables
                    - **RF32–RF40** Administración de roles y categorías
                    """)
                .version("1.0.0")
                .contact(new Contact().name("Equipo SIENEP — UTEC").email("paulo.vaccaro@estudiantes.utec.edu.uy")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
