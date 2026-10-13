package pe.edu.unsch.clientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;

/**
 * Aplicación principal de la API REST de Clientes
 * Lab 04 - IS-388 Construcción y Evolución de Software
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "API Clientes",
        version = "1.0.0",
        description = "Sistema de gestión de clientes - Lab 04 IS-388",
        contact = @Contact(
            name = "UNSCH",
            email = "info@unsch.edu.pe"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    )
)
public class ClientesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ClientesApplication.class, args);
    }
}
