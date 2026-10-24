package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    List<HistoriaClinica> findByPacienteIdOrderByFechaAtencionDesc(Long pacienteId);

    Optional<HistoriaClinica> findByTurnoId(Long turnoId);
}
