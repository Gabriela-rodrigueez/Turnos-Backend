package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.ProfesionalDTO;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesionalServiceImpl implements ProfesionalService {

    private final ProfesionalRepository profesionalRepository;

    public ProfesionalServiceImpl(ProfesionalRepository profesionalRepository) {
        this.profesionalRepository = profesionalRepository;
    }

    @Override
    public List<ProfesionalDTO> listarProfesionales(Long especialidadId, Long sedeId) {
        List<Profesional> profesionales;

        if (especialidadId != null) {
            profesionales = profesionalRepository.findByEspecialidadesId(especialidadId);
        } else {
            profesionales = profesionalRepository.findAll();
        }

        return profesionales.stream()
                .map(p -> {
                    String especialidadesStr = p.getEspecialidades().stream()
                            .map(Especialidad::getNombre)
                            .collect(Collectors.joining(", "));
                    return new ProfesionalDTO(
                            p.getId(),
                            p.getNombre(),
                            p.getApellido(),
                            especialidadesStr,
                            "Sede Principal"
                    );
                })
                .collect(Collectors.toList());
    }
}