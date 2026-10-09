package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCuil(String cuil);
    Optional<Paciente> findByEmail(String email);
    List<Paciente> findByTutorId(Long tutorId);

    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(p.nombre, ' ', p.apellido)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(p.apellido, ' ', p.nombre)) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    List<Paciente> buscarPorNombreOApellido(@Param("filtro") String filtro);
}
