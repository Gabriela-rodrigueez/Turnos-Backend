package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    
    Optional<Sede> findByNombreIgnoreCase(String nombre);
    
    List<Sede> findByCiudadIgnoreCase(String ciudad);

    // Nueva consulta que busca las sedes vinculadas a los turnos del profesional
    @Query(value = "SELECT DISTINCT s.* FROM sedes s INNER JOIN turnos t ON s.id = t.sede_id WHERE t.profesional_id = :profesionalId", nativeQuery = true)
    List<Sede> findSedesByProfesionalId(@Param("profesionalId") Long profesionalId);
}