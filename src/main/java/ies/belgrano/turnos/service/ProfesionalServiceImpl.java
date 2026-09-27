package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.ProfesionalDTO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfesionalServiceImpl implements ProfesionalService {
    
    @Override
    public List<ProfesionalDTO> listarProfesionales(Long especialidadId, Long sedeId) {
        // retornamos una lista vacía para el código 204 No Content
        return new ArrayList<>();
    }
}