package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.ConflictoHorarioException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.*;
import ies.belgrano.turnos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadRepository especialidadRepository;
    private final SedeRepository sedeRepository;

    public TurnoServiceImpl(TurnoRepository turnoRepository,
                            PacienteRepository pacienteRepository,
                            ProfesionalRepository profesionalRepository,
                            EspecialidadRepository especialidadRepository,
                            SedeRepository sedeRepository) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profesionalRepository = profesionalRepository;
        this.especialidadRepository = especialidadRepository;
        this.sedeRepository = sedeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> consultarDisponibilidad(Long especialidadId, Long profesionalId, Long sedeId, LocalDate fecha) {
        List<Turno> turnos = turnoRepository.findAll();

        return turnos.stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .filter(t -> especialidadId == null || (t.getEspecialidad() != null && t.getEspecialidad().getId().equals(especialidadId)))
                .filter(t -> profesionalId == null || (t.getProfesional() != null && t.getProfesional().getId().equals(profesionalId)))
                .filter(t -> sedeId == null || (t.getSede() != null && t.getSede().getId().equals(sedeId)))
                .filter(t -> {
                    if (fecha == null) return true;
                    LocalDateTime inicioDia = fecha.atStartOfDay();
                    LocalDateTime finDia = fecha.atTime(LocalTime.MAX);
                    return !t.getFechaHora().isBefore(inicioDia) && !t.getFechaHora().isAfter(finDia);
                })
                .map(TurnoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TurnoResponseDTO reservarTurno(ReservaTurnoRequestDTO request) {
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paciente con ID: " + request.getPacienteId()));

        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesional con ID: " + request.getProfesionalId()));

        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la especialidad con ID: " + request.getEspecialidadId()));

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la sede con ID: " + request.getSedeId()));

        // Regla de Negocio: Impide dos turnos activos para el mismo profesional en la misma fecha y hora
        boolean existeConflicto = turnoRepository.existsByProfesionalIdAndFechaHoraAndEstadoNot(
                request.getProfesionalId(),
                request.getFechaHora(),
                EstadoTurno.CANCELADO
        );

        if (existeConflicto) {
            throw new ConflictoHorarioException("El profesional seleccionado ya posee un turno agendado en el horario: " + request.getFechaHora());
        }

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setEspecialidad(especialidad);
        turno.setSede(sede);
        turno.setFechaHora(request.getFechaHora());
        turno.setMotivoConsulta(request.getMotivoConsulta());
        turno.setEstado(EstadoTurno.RESERVADO);

        Turno turnoGuardado = turnoRepository.save(turno);
        return TurnoResponseDTO.fromEntity(turnoGuardado);
    }

    @Override
    @Transactional
    public TurnoResponseDTO cancelarTurno(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el turno con ID: " + id));

        turno.setEstado(EstadoTurno.CANCELADO);
        Turno turnoActualizado = turnoRepository.save(turno);

        return TurnoResponseDTO.fromEntity(turnoActualizado);
    }
}
