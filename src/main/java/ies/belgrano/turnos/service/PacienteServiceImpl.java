package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.security.AuthenticatedUser;
import ies.belgrano.turnos.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}