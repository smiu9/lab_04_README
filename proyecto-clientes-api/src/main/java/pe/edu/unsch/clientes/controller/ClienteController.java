package pe.edu.unsch.clientes.controller;

import pe.edu.unsch.clientes.service.ClienteService;
import pe.edu.unsch.clientes.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller para gestión de clientes
 */
@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes")
public class ClienteController {
    
    private final ClienteService service;
    
    public ClienteController(ClienteService service) {
        this.service = service;
    }
    
    /**
     * Crear un nuevo cliente
     */
    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Registra un nuevo cliente en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "DNI ya existe")
    })
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO resultado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
    
    /**
     * Obtener cliente por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }
    
    /**
     * Listar clientes o buscar por DNI/nombre
     */
    @GetMapping
    @Operation(summary = "Listar clientes o buscar", 
               description = "Obtiene lista de clientes o busca por DNI (?dni=) o nombre (?nombre=)")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    public ResponseEntity<Object> listar(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String nombre) {
        
        if (dni != null && !dni.isEmpty()) {
            return ResponseEntity.ok(service.buscarPorDni(dni));
        } else if (nombre != null && !nombre.isEmpty()) {
            return ResponseEntity.ok(service.buscarPorNombre(nombre));
        } else {
            return ResponseEntity.ok(service.obtenerTodos());
        }
    }
    
    /**
     * Actualizar cliente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }
    
    /**
     * Eliminar cliente
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
