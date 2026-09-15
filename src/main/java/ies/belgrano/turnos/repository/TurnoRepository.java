package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByPacienteId(Long pacienteId);
    List<Turno> findByProfesionalId(Long profesionalId);
    List<Turno> findByEstado(EstadoTurno estado);
    List<Turno> findBySedeIdAndEspecialidadId(Long sedeId, Long especialidadId);
    List<Turno> findByProfesionalIdAndFechaHoraBetween(Long profesionalId, LocalDateTime inicio, LocalDateTime fin);
    List<Turno> findBySedeIdAndEspecialidadIdAndEstado(Long sedeId, Long especialidadId, EstadoTurno estado);
}
