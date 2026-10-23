package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.PacienteResponseDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.security.AuthenticatedUser;
import ies.belgrano.turnos.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;

    public PacienteServiceImpl(PacienteRepository pacienteRepository,
                               TurnoRepository turnoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> obtenerTurnosPorPaciente(Long pacienteId) {
        // Regla de Negocio: Si el usuario autenticado tiene rol PACIENTE, se fuerza su propio pacienteId
        Long targetPacienteId = SecurityUtils.getCurrentUser()
                .filter(user -> user.esPaciente() && user.getPacienteId() != null)
                .map(AuthenticatedUser::getPacienteId)
                .orElse(pacienteId);

        if (!pacienteRepository.existsById(targetPacienteId)) {
            throw new RecursoNoEncontradoException("No se encontró el paciente con ID: " + targetPacienteId);
        }

        List<Turno> turnos = turnoRepository.findByPacienteId(targetPacienteId);

        return turnos.stream()
                .map(TurnoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI de búsqueda no puede ser nulo o vacío.");
        }

        Paciente paciente = pacienteRepository.findByDni(dni.trim())
                
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ningún paciente con el DNI: " + dni.trim()));

        return PacienteResponseDTO.fromEntity(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarPacientes(String filtro) {
        List<Paciente> pacientes = pacienteRepository.findAll();

        if (filtro != null && !filtro.trim().isEmpty()) {
            String filtroNormalizado = normalizarTexto(filtro.trim());
            pacientes = pacientes.stream()
                    .filter(p -> {
                        String nombreNorm = normalizarTexto(p.getNombre());
                        String apellidoNorm = normalizarTexto(p.getApellido());
                        String nombreCompleto1 = nombreNorm + " " + apellidoNorm;
                        String nombreCompleto2 = apellidoNorm + " " + nombreNorm;
                        return nombreNorm.contains(filtroNormalizado)
                                || apellidoNorm.contains(filtroNormalizado)
                                || nombreCompleto1.contains(filtroNormalizado)
                                || nombreCompleto2.contains(filtroNormalizado);
                    })
                    .collect(Collectors.toList());
        }

        return pacientes.stream()
                .map(PacienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

        //Grupo Familiar
    @Override
    @Transactional
    public PacienteResponseDTO registrarFamiliar(Long tutorId, Paciente familiarDto) {
        // 1. Verificar que el tutor exista
        Paciente tutor = pacienteRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el tutor con ID: " + tutorId));

        // 2. Configurar el familiar
        familiarDto.setTutor(tutor);
        familiarDto.setIsActive(true); 

        Paciente familiarGuardado = pacienteRepository.save(familiarDto);
        return PacienteResponseDTO.fromEntity(familiarGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarFamiliaresActivos(Long tutorId) {
        // Verificar que el tutor exista
        if (!pacienteRepository.existsById(tutorId)) {
            throw new RecursoNoEncontradoException("No se encontró el tutor con ID: " + tutorId);
        }

        List<Paciente> familiares = pacienteRepository.findByTutorIdAndIsActiveTrue(tutorId);
        return familiares.stream()
                .map(PacienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void bajaLogicaFamiliar(Long tutorId, Long familiarId) {
        Paciente familiar = pacienteRepository.findById(familiarId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el familiar con ID: " + familiarId));

        // Validar que pertenezca al tutor indicado
        if (familiar.getTutor() == null || !familiar.getTutor().getId().equals(tutorId)) {
            throw new IllegalArgumentException("El paciente no pertenece al tutor especificado.");
        }

        // Aplicar baja lógica (regla de negocio: isAcive = false)
        familiar.setIsActive(false);
        
        pacienteRepository.save(familiar);
    }

    @Override
    @Transactional
    public void reactivarFamiliar(Long tutorId, Long familiarId) {
        Paciente familiar = pacienteRepository.findById(familiarId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el familiar con ID: " + familiarId));

        // Validar pertenencia al tutor
        if (familiar.getTutor() == null || !familiar.getTutor().getId().equals(tutorId)) {
            throw new IllegalArgumentException("El paciente no pertenece al tutor especificado.");
        }

        // Reactivar
        familiar.setIsActive(true);
        pacienteRepository.save(familiar);
    }

}