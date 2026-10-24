package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.HistoriaClinicaResponseDTO;
import ies.belgrano.turnos.dto.RegistrarAtencionRequestDTO;

import java.util.List;

public interface HistoriaClinicaService {

    HistoriaClinicaResponseDTO registrarAtencion(Long turnoId, RegistrarAtencionRequestDTO request);

    List<HistoriaClinicaResponseDTO> obtenerHistorialPorPaciente(Long pacienteId);

    List<HistoriaClinicaResponseDTO> obtenerMiHistorial();

    HistoriaClinicaResponseDTO obtenerRegistroPorId(Long registroId);
}
