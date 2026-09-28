package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.SedeDTO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service 
public class SedeServiceImpl implements SedeService {
    
    @Override
    public List<SedeDTO> listarSedes() {
        
       // lista vacía para probar el error "204 No Content".
        return new ArrayList<>(); 
    }
}