package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadServiceImpl(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public List<EspecialidadDTO> listarEspecialidades() {
        return especialidadRepository.findAll().stream()
                .map(esp -> new EspecialidadDTO(esp.getId(), esp.getNombre()))
                .collect(Collectors.toList());
    }
}