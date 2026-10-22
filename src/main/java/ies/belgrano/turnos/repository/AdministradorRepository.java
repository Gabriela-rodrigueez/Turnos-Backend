package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    
    // Método para buscar al administrador por su legajo
    Optional<Administrador> findByLegajo(String legajo);
    
    // Método para verificar si un legajo ya existe (útil por si en el futuro agregas más admins)
    boolean existsByLegajo(String legajo);
}