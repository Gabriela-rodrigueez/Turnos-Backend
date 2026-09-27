package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class EspecialidadServiceImpl implements EspecialidadService {
    
    @Override
    public List<EspecialidadDTO> listarEspecialidades() {
        // Retornamos una lista vacía para forzar el código HTTP 204
        return new ArrayList<>(); 
    }
}