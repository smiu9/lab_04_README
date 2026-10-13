package pe.edu.unsch.clientes.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para recibir datos de cliente en las peticiones HTTP
 */
public class ClienteRequestDTO {
    
    @NotBlank(message = "DNI es requerido")
    @Size(min = 8, max = 8, message = "DNI debe tener exactamente 8 dígitos")
    private String dni;
    
    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    private String email;
    
    @Pattern(regexp = "^[0-9\\-]*$", message = "Teléfono inválido")
    private String telefono;
    
    // Constructores
    public ClienteRequestDTO() {}
    
    public ClienteRequestDTO(String dni, String nombre, String email) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
    }
    
    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
