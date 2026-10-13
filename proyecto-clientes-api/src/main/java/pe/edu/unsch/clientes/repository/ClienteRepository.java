package pe.edu.unsch.clientes.repository;

import pe.edu.unsch.clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA para operaciones CRUD de Cliente
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    /**
     * Busca un cliente por su DNI (único)
     */
    Optional<Cliente> findByDni(String dni);
    
    /**
     * Busca clientes cuyo nombre contenga el texto especificado (case-insensitive)
     */
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}
