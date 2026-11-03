package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.dto.EspecialidadRequestDTO;
import ies.belgrano.turnos.dto.EspecialidadResponseDTO;

import java.util.List;

public interface EspecialidadService {
    List<EspecialidadDTO> listarEspecialidades();
    List<EspecialidadDTO> listarEspecialidades(Long profesionalId);
    EspecialidadResponseDTO crearEspecialidad(EspecialidadRequestDTO request);
    EspecialidadResponseDTO actualizarEspecialidad(Long id, EspecialidadRequestDTO request);
    EspecialidadResponseDTO obtenerEspecialidadPorId(Long id);
}