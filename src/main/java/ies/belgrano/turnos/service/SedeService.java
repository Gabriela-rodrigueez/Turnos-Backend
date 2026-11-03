package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.SedeDTO;
import java.util.List;

public interface SedeService {
    // método que devuelve una lista de SedeDTO.
    List<SedeDTO> listarSedes();

    // Nuevo método para filtrar sedes por el ID del profesional según sus turnos
    List<SedeDTO> obtenerSedesPorProfesional(Long profesionalId);
}