package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByDni(String dni);
    Optional<Paciente> findByEmail(String email);
    List<Paciente> findByTutorId(Long tutorId);
}
