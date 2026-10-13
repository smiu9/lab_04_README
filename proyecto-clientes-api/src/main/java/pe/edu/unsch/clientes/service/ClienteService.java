package pe.edu.unsch.clientes.service;

import pe.edu.unsch.clientes.model.Cliente;
import pe.edu.unsch.clientes.repository.ClienteRepository;
import pe.edu.unsch.clientes.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que contiene la lógica de negocio para gestión de clientes
 */
@Service
@Transactional
public class ClienteService {
    
    private final ClienteRepository repository;
    private final ClienteMapper mapper;
    
    /**
     * Constructor con inyección de dependencias
     */
    public ClienteService(ClienteRepository repository, ClienteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    /**
     * Crea un nuevo cliente
     * @throws IllegalArgumentException si el DNI ya existe
     */
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        // Validar que el DNI no exista
        if (repository.findByDni(dto.getDni()).isPresent()) {
            throw new IllegalArgumentException("DNI ya existe");
        }
        
        Cliente cliente = mapper.toEntity(dto);
        cliente = repository.save(cliente);
        return mapper.toDTO(cliente);
    }
    
    /**
     * Obtiene un cliente por su ID
     */
    public ClienteResponseDTO obtenerPorId(Long id) {
        Cliente cliente = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + id + " no encontrado"));
        return mapper.toDTO(cliente);
    }
    
    /**
     * Obtiene todos los clientes
     */
    public List<ClienteResponseDTO> obtenerTodos() {
        return repository.findAll()
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Busca un cliente por DNI
     */
    public ClienteResponseDTO buscarPorDni(String dni) {
        Cliente cliente = repository.findByDni(dni)
            .orElseThrow(() -> new IllegalArgumentException("Cliente con DNI " + dni + " no encontrado"));
        return mapper.toDTO(cliente);
    }
    
    /**
     * Busca clientes por nombre (parcial)
     */
    public List<ClienteResponseDTO> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Actualiza un cliente existente
     */
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + id + " no encontrado"));
        
        mapper.updateEntity(dto, cliente);
        cliente = repository.save(cliente);
        return mapper.toDTO(cliente);
    }
    
    /**
     * Elimina un cliente
     */
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Cliente con ID " + id + " no encontrado");
        }
        repository.deleteById(id);
    }
}
