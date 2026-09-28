package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.ProfesionalDTO;
import java.util.List;

public interface ProfesionalService {
    // Método que permite listar profesionales filtrando opcionalmente por especialidad y/o sede
    List<ProfesionalDTO> listarProfesionales(Long especialidadId, Long sedeId);
}