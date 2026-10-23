package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalRepository profesionalRepository;

    public EspecialidadServiceImpl(EspecialidadRepository especialidadRepository, ProfesionalRepository profesionalRepository) {
        this.especialidadRepository = especialidadRepository;
        this.profesionalRepository = profesionalRepository;
    }

    @Override
    public List<EspecialidadDTO> listarEspecialidades() {
        return listarEspecialidades(null);
    }

    @Override
    public List<EspecialidadDTO> listarEspecialidades(Long profesionalId) {
        if (profesionalId != null) {
            return profesionalRepository.findById(profesionalId)
                    .map(prof -> prof.getEspecialidades().stream()
                            .map(esp -> new EspecialidadDTO(esp.getId(), esp.getNombre()))
                            .sorted(Comparator.comparing(EspecialidadDTO::getId))
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        }

        return especialidadRepository.findAll().stream()
                .map(esp -> new EspecialidadDTO(esp.getId(), esp.getNombre()))
                .sorted(Comparator.comparing(EspecialidadDTO::getId))
                .collect(Collectors.toList());
    }
}