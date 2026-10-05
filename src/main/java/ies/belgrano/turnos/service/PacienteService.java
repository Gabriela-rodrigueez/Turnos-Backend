package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.PacienteResponseDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;

import java.util.List;

public interface PacienteService {
    List<TurnoResponseDTO> obtenerTurnosPorPaciente(Long pacienteId);
    default List<TurnoResponseDTO> listarTurnosPorPaciente(Long pacienteId) {
        return obtenerTurnosPorPaciente(pacienteId);
    }
    PacienteResponseDTO buscarPorDni(String dni);
    List<PacienteResponseDTO> listarPacientes(String filtro);
}