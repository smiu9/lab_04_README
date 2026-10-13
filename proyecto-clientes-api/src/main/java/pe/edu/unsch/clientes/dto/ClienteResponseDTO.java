package pe.edu.unsch.clientes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * DTO para enviar datos de cliente en las respuestas HTTP
 */
public class ClienteResponseDTO {
    
    private Long id;
    private String dni;
    private String nombre;
    private String email;
    private String telefono;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date createdAt;
    
    // Constructores
    public ClienteResponseDTO() {}
    
    public ClienteResponseDTO(Long id, String dni, String nombre, String email, String telefono, Date createdAt) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.createdAt = createdAt;
    }
    
    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
