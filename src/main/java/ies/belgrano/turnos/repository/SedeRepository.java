package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    Optional<Sede> findByNombreIgnoreCase(String nombre);
    List<Sede> findByCiudadIgnoreCase(String ciudad);

    @Query("SELECT DISTINCT t.sede FROM Turno t WHERE t.profesional.id = :profesionalId ORDER BY t.sede.nombre ASC")
    List<Sede> findDistinctByTurnosProfesionalId(@Param("profesionalId") Long profesionalId);
}
