package ies.belgrano.turnos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ies.belgrano.turnos.dto.HistoriaClinicaResponseDTO;
import ies.belgrano.turnos.dto.RegistrarAtencionRequestDTO;
import ies.belgrano.turnos.exception.AccesoDenegadoException;
import ies.belgrano.turnos.exception.NoAutorizadoException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.HistoriaClinica;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.HistoriaClinicaRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.security.AuthenticatedUser;
import ies.belgrano.turnos.security.SecurityUtils;

@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;

    public HistoriaClinicaServiceImpl(HistoriaClinicaRepository historiaClinicaRepository,
                                      TurnoRepository turnoRepository,
                                      PacienteRepository pacienteRepository) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    @Transactional
    public HistoriaClinicaResponseDTO registrarAtencion(Long turnoId, RegistrarAtencionRequestDTO request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new NoAutorizadoException("No hay una sesión activa en el sistema"));

        if (!currentUser.esMedico() || currentUser.getProfesionalId() == null) {
            throw new AccesoDenegadoException("Solo los usuarios con rol MEDICO pueden registrar la atención de una consulta.");
        }

        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con ID: " + turnoId));

        if (!turno.getProfesional().getId().equals(currentUser.getProfesionalId())) {
            throw new AccesoDenegadoException("No tienes autorización para atender un turno asignado a otro profesional médico.");
        }

        if (turno.getPaciente() == null) {
            throw new IllegalArgumentException("El turno no tiene un paciente asignado para registrar su atención.");
        }

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new IllegalArgumentException("No se puede registrar atención médica sobre un turno cancelado.");
        }

        if (turno.getEstado() == EstadoTurno.DISPONIBLE) {
            throw new IllegalArgumentException("No se puede registrar atención médica sobre un turno libre/no reservado.");
        }

        // Se marca el turno como ASISTIDO al finalizar la atención médica
        turno.setEstado(EstadoTurno.ASISTIDO);
        turnoRepository.save(turno);

        // Se persiste el registro de evolución en el historial clínico del paciente
        HistoriaClinica historia = new HistoriaClinica();
        historia.setPaciente(turno.getPaciente());
        historia.setProfesional(turno.getProfesional());
        historia.setTurno(turno);
        historia.setFechaAtencion(LocalDateTime.now());
        historia.setMotivoConsulta(request.getMotivoConsulta());
        historia.setDiagnostico(request.getDiagnostico());
        historia.setTratamiento(request.getTratamiento());
        historia.setRecetasPrescritas(request.getRecetasPrescritas());
        historia.setEstudiosSolicitados(request.getEstudiosSolicitados());

        HistoriaClinica guardada = historiaClinicaRepository.save(historia);
        return mapearADTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinicaResponseDTO> obtenerHistorialPorPaciente(Long pacienteId) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new NoAutorizadoException("No hay una sesión activa en el sistema"));

        // Regla de Ownership estricta (AGENTS.md):
        // Paciente solo puede ver sus propios registros
        if (currentUser.esPaciente()) {
            if (!pacienteId.equals(currentUser.getPacienteId())) {
                throw new AccesoDenegadoException("Acceso denegado: No tienes autorización para consultar el historial clínico de otro paciente.");
            }
        }

        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RecursoNoEncontradoException("Paciente no encontrado con ID: " + pacienteId);
        }

        return historiaClinicaRepository.findByPacienteIdOrderByFechaAtencionDesc(pacienteId)
                .stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriaClinicaResponseDTO> obtenerMiHistorial() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new NoAutorizadoException("No hay una sesión activa en el sistema"));

        if (!currentUser.esPaciente() || currentUser.getPacienteId() == null) {
            throw new AccesoDenegadoException("Este endpoint es de uso exclusivo para usuarios autenticados con rol PACIENTE.");
        }

        return obtenerHistorialPorPaciente(currentUser.getPacienteId());
    }

    @Override
    @Transactional(readOnly = true)
    public HistoriaClinicaResponseDTO obtenerRegistroPorId(Long registroId) {
        HistoriaClinica historia = historiaClinicaRepository.findById(registroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro clínico no encontrado con ID: " + registroId));

        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new NoAutorizadoException("No hay una sesión activa en el sistema"));

        if (currentUser.esPaciente()) {
            if (!historia.getPaciente().getId().equals(currentUser.getPacienteId())) {
                throw new AccesoDenegadoException("Acceso denegado: No tienes autorización para consultar este registro clínico.");
            }
        }

        return mapearADTO(historia);
    }

    private HistoriaClinicaResponseDTO mapearADTO(HistoriaClinica h) {
        HistoriaClinicaResponseDTO dto = new HistoriaClinicaResponseDTO();
        dto.setId(h.getId());

        if (h.getPaciente() != null) {
            dto.setPacienteId(h.getPaciente().getId());
            dto.setPacienteNombreCompleto(h.getPaciente().getNombre() + " " + h.getPaciente().getApellido());
            dto.setPacienteDni(h.getPaciente().getDni());
        }

        if (h.getProfesional() != null) {
            dto.setProfesionalId(h.getProfesional().getId());
            dto.setProfesionalNombreCompleto(h.getProfesional().getNombre() + " " + h.getProfesional().getApellido());
            dto.setProfesionalMatricula(h.getProfesional().getMatricula());
        }

        if (h.getTurno() != null) {
            dto.setTurnoId(h.getTurno().getId());
            if (h.getTurno().getEspecialidad() != null) {
                dto.setEspecialidadNombre(h.getTurno().getEspecialidad().getNombre());
            }
            if (h.getTurno().getSede() != null) {
                dto.setSedeNombre(h.getTurno().getSede().getNombre());
            }
        }

        dto.setFechaAtencion(h.getFechaAtencion());
        dto.setMotivoConsulta(h.getMotivoConsulta());
        dto.setDiagnostico(h.getDiagnostico());
        dto.setTratamiento(h.getTratamiento());
        dto.setRecetasPrescritas(h.getRecetasPrescritas());
        dto.setEstudiosSolicitados(h.getEstudiosSolicitados());
        return dto;
    }
}
