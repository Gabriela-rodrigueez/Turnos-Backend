package ies.belgrano.turnos.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ies.belgrano.turnos.dto.CambiarEstadoTurnoRequestDTO;
import ies.belgrano.turnos.dto.GeneracionSlotsResponseDTO;
import ies.belgrano.turnos.dto.GenerarSlotsRequestDTO;
import ies.belgrano.turnos.dto.ReservaPresencialRequestDTO;
import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.AccesoDenegadoException;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.ConflictoHorarioException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.security.SecurityUtils;

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
    @Transactional
    public GeneracionSlotsResponseDTO generarSlots(GenerarSlotsRequestDTO request) {
        if (request.getFechaHasta().isBefore(request.getFechaDesde())) {
            throw new ConflictoException("La fecha hasta no puede ser anterior a la fecha desde.");
        }
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new ConflictoException("La hora fin debe ser posterior a la hora inicio.");
        }

        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el profesional con ID: " + request.getProfesionalId()));

        // Ownership Check: Si es MÉDICO, solo puede generar slots para su propio profesionalId
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (user.esMedico()) {
                if (user.getProfesionalId() == null || !user.getProfesionalId().equals(request.getProfesionalId())) {
                    throw new AccesoDenegadoException("No tiene permisos para generar slots para otro profesional.");
                }
            }
        });

        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la especialidad con ID: " + request.getEspecialidadId()));

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la sede con ID: " + request.getSedeId()));

        List<DayOfWeek> diasSemana = (request.getDiasSemana() != null && !request.getDiasSemana().isEmpty())
                ? request.getDiasSemana()
                : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

        List<Turno> nuevosSlots = new ArrayList<>();
        LocalDate actual = request.getFechaDesde();

        while (!actual.isAfter(request.getFechaHasta())) {
            if (diasSemana.contains(actual.getDayOfWeek())) {
                LocalTime horaSlotInicio = request.getHoraInicio();
                while (true) {
                    LocalTime horaSlotFin = horaSlotInicio.plusMinutes(request.getDuracionMinutos());
                    if (horaSlotFin.isAfter(request.getHoraFin())) {
                        break;
                    }

                    LocalDateTime slotInicio = actual.atTime(horaSlotInicio);
                    LocalDateTime slotFin = actual.atTime(horaSlotFin);

                    // Validación estricta de no-solapamiento con turnos activos en base de datos
                    List<Turno> solapamientos = turnoRepository.findSolapamientos(
                            profesional.getId(),
                            slotInicio,
                            slotFin,
                            EstadoTurno.CANCELADO
                    );

                    if (!solapamientos.isEmpty()) {
                        Turno conflicto = solapamientos.get(0);
                        throw new ConflictoHorarioException(
                                String.format("Conflicto de solapamiento de agenda: El profesional ya posee un turno agendado en el rango [%s - %s] (Turno ID: %d, Horario existente: %s a %s, Estado: %s).",
                                        slotInicio, slotFin, conflicto.getId(), conflicto.getFechaHoraInicio(), conflicto.getFechaHoraFin(), conflicto.getEstado())
                        );
                    }

                    Turno slot = new Turno();
                    slot.setProfesional(profesional);
                    slot.setEspecialidad(especialidad);
                    slot.setSede(sede);
                    slot.setFechaHoraInicio(slotInicio);
                    slot.setFechaHoraFin(slotFin);
                    slot.setFechaHora(slotInicio);
                    slot.setEstado(EstadoTurno.DISPONIBLE);
                    slot.setPaciente(null);

                    nuevosSlots.add(slot);
                    horaSlotInicio = horaSlotFin;
                }
            }
            actual = actual.plusDays(1);
        }

        List<Turno> guardados = turnoRepository.saveAll(nuevosSlots);
        List<TurnoResponseDTO> slotDtos = guardados.stream()
                .map(TurnoResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new GeneracionSlotsResponseDTO(
                guardados.size(),
                profesional.getId(),
                profesional.getNombre() + " " + profesional.getApellido(),
                especialidad.getId(),
                especialidad.getNombre(),
                sede.getId(),
                sede.getNombre(),
                request.getFechaDesde(),
                request.getFechaHasta(),
                slotDtos
        );
    }

    @Override
    @Transactional
    public void eliminarSlot(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el slot con ID: " + id));

        // Ownership Check: Si es MÉDICO, solo puede eliminar sus propios slots
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (user.esMedico()) {
                if (user.getProfesionalId() == null || turno.getProfesional() == null || !user.getProfesionalId().equals(turno.getProfesional().getId())) {
                    throw new AccesoDenegadoException("No tiene permisos para eliminar slots de otro profesional.");
                }
            }
        });

        if (turno.getEstado() == EstadoTurno.RESERVADO) {
            throw new ConflictoException("No es posible eliminar el slot con ID " + id + " porque se encuentra en estado RESERVADO.");
        }
        if (turno.getEstado() != EstadoTurno.DISPONIBLE) {
            throw new ConflictoException("Solo se pueden eliminar slots en estado DISPONIBLE (Estado actual: " + turno.getEstado() + ").");
        }

        turnoRepository.delete(turno);
    }

    @Override
    @Transactional
    public TurnoResponseDTO cambiarEstado(Long id, CambiarEstadoTurnoRequestDTO request) {
        if (request == null || request.getEstado() == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio.");
        }
        EstadoTurno nuevoEstado = request.getEstado();
        if (nuevoEstado != EstadoTurno.ASISTIDO && nuevoEstado != EstadoTurno.CANCELADO && nuevoEstado != EstadoTurno.RESERVADO) {
            throw new ConflictoException("Transición no permitida. Los estados válidos para modificación son: ASISTIDO, CANCELADO o RESERVADO.");
        }

        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el turno con ID: " + id));

        // Ownership Check: Si es MÉDICO, solo puede cambiar estado a turnos asignados a él
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (user.esMedico()) {
                if (user.getProfesionalId() == null || turno.getProfesional() == null || !user.getProfesionalId().equals(turno.getProfesional().getId())) {
                    throw new AccesoDenegadoException("No tiene permisos para modificar el estado de un turno asignado a otro profesional.");
                }
            }
        });

        turno.setEstado(nuevoEstado);
        if (request.getObservaciones() != null) {
            turno.setObservaciones(request.getObservaciones());
        }
        Turno turnoActualizado = turnoRepository.save(turno);

        return TurnoResponseDTO.fromEntity(turnoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> consultarDisponibilidad(Long especialidadId, Long profesionalId, Long sedeId, LocalDate fecha) {
        LocalDateTime inicioDia = fecha != null ? fecha.atStartOfDay() : null;
        LocalDateTime finDia = fecha != null ? fecha.atTime(LocalTime.MAX) : null;

        List<Turno> turnosDisponibles = turnoRepository.findDisponibles(
                EstadoTurno.DISPONIBLE,
                especialidadId,
                profesionalId,
                sedeId,
                inicioDia,
                finDia
        );

        return turnosDisponibles.stream()
                .map(TurnoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TurnoResponseDTO reservarTurno(ReservaTurnoRequestDTO request) {
        // Regla de Negocio: Si el usuario autenticado posee rol PACIENTE, se asigna su propio pacienteId
        final Long[] pacienteIdHolder = new Long[]{request.getPacienteId()};
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (user.esPaciente() && user.getPacienteId() != null) {
                pacienteIdHolder[0] = user.getPacienteId();
            }
        });

        Long pacienteId = pacienteIdHolder[0];
        if (pacienteId == null) {
            throw new IllegalArgumentException("El ID del paciente es obligatorio para efectuar la reserva.");
        }

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paciente con ID: " + pacienteId));

        Turno turno;
        if (request.getTurnoId() != null) {
            turno = turnoRepository.findById(request.getTurnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el turno con ID: " + request.getTurnoId()));
        } else if (request.getProfesionalId() != null && request.getFechaHora() != null) {
            turno = turnoRepository.findFirstByProfesionalIdAndFechaHoraInicio(request.getProfesionalId(), request.getFechaHora())
                    .or(() -> turnoRepository.findFirstByProfesionalIdAndFechaHora(request.getProfesionalId(), request.getFechaHora()))
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un slot disponible para el profesional y horario especificado."));
        } else {
            throw new IllegalArgumentException("Debe especificar el turnoId a reservar.");
        }

        if (turno.getEstado() != EstadoTurno.DISPONIBLE) {
            throw new ConflictoException("El turno no se encuentra en estado DISPONIBLE (Estado actual: " + turno.getEstado() + ").");
        }

        turno.setPaciente(paciente);
        turno.setEstado(EstadoTurno.RESERVADO);
        if (request.getMotivoConsulta() != null && !request.getMotivoConsulta().isBlank()) {
            turno.setMotivoConsulta(request.getMotivoConsulta());
        }

        Turno turnoGuardado = turnoRepository.save(turno);
        return TurnoResponseDTO.fromEntity(turnoGuardado);
    }

    @Override
    @Transactional
    public TurnoResponseDTO reservarTurnoPresencial(ReservaPresencialRequestDTO request) {
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el paciente con ID: " + request.getPacienteId()));

        Turno turno;
        if (request.getTurnoId() != null) {
            turno = turnoRepository.findById(request.getTurnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el turno con ID: " + request.getTurnoId()));
        } else if (request.getProfesionalId() != null && request.getFechaHora() != null) {
            turno = turnoRepository.findFirstByProfesionalIdAndFechaHoraInicio(request.getProfesionalId(), request.getFechaHora())
                    .or(() -> turnoRepository.findFirstByProfesionalIdAndFechaHora(request.getProfesionalId(), request.getFechaHora()))
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un slot disponible para el profesional y horario especificado."));
        } else {
            throw new IllegalArgumentException("Debe especificar el turnoId a reservar.");
        }

        if (turno.getEstado() != EstadoTurno.DISPONIBLE) {
            throw new ConflictoException("El turno no se encuentra en estado DISPONIBLE (Estado actual: " + turno.getEstado() + ").");
        }

        turno.setPaciente(paciente);
        turno.setEstado(EstadoTurno.RESERVADO);
        if (request.getMotivoConsulta() != null && !request.getMotivoConsulta().isBlank()) {
            turno.setMotivoConsulta(request.getMotivoConsulta());
        }
        if (request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            turno.setObservaciones(request.getObservaciones());
        }

        Turno turnoGuardado = turnoRepository.save(turno);
        return TurnoResponseDTO.fromEntity(turnoGuardado);
    }

    @Override
    @Transactional
    public TurnoResponseDTO cancelarTurno(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el turno con ID: " + id));

        // Validación de propiedad: Pacientes y Médicos solo pueden cancelar sus propios turnos
        SecurityUtils.getCurrentUser().ifPresent(user -> {
            if (user.esPaciente()) {
                if (user.getPacienteId() == null || turno.getPaciente() == null || !user.getPacienteId().equals(turno.getPaciente().getId())) {
                    throw new AccessDeniedException("No tiene permiso para cancelar este turno.");
                }
            } else if (user.esMedico()) {
                if (user.getProfesionalId() == null || turno.getProfesional() == null || !user.getProfesionalId().equals(turno.getProfesional().getId())) {
                    throw new AccessDeniedException("No tiene permiso para cancelar un turno asignado a otro profesional.");
                }
            }
        });

        turno.setEstado(EstadoTurno.CANCELADO);
        Turno turnoActualizado = turnoRepository.save(turno);

        return TurnoResponseDTO.fromEntity(turnoActualizado);
    }
}
