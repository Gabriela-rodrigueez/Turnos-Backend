package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;

    public PacienteServiceImpl(PacienteRepository pacienteRepository, TurnoRepository turnoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> obtenerTurnosPorPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RecursoNoEncontradoException("No se encontró el paciente con ID: " + pacienteId);
        }

        List<Turno> turnos = turnoRepository.findByPacienteId(pacienteId);

        return turnos.stream()
                .map(TurnoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}