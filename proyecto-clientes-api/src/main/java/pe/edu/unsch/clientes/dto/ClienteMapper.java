package pe.edu.unsch.clientes.dto;

import pe.edu.unsch.clientes.model.Cliente;
import org.springframework.stereotype.Component;

/**
 * Mapper que convierte entre entidad Cliente y DTOs
 */
@Component
public class ClienteMapper {
    
    /**
     * Convierte ClienteRequestDTO a entidad Cliente
     */
    public Cliente toEntity(ClienteRequestDTO dto) {
        if (dto == null) return null;
        
        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        return cliente;
    }
    
    /**
     * Convierte entidad Cliente a ClienteResponseDTO
     */
    public ClienteResponseDTO toDTO(Cliente cliente) {
        if (cliente == null) return null;
        
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getDni(),
            cliente.getNombre(),
            cliente.getEmail(),
            cliente.getTelefono(),
            cliente.getCreatedAt()
        );
    }
    
    /**
     * Actualiza una entidad Cliente con datos de ClienteRequestDTO
     */
    public void updateEntity(ClienteRequestDTO dto, Cliente cliente) {
        if (dto == null || cliente == null) return;
        
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
    }
}
