package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.dto.EspecialidadRequestDTO;
import ies.belgrano.turnos.dto.EspecialidadResponseDTO;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listarEspecialidades() {
        return listarEspecialidades(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listarEspecialidades(Long profesionalId) {
        if (profesionalId != null) {
            return profesionalRepository.findById(profesionalId)
                    .map(prof -> prof.getEspecialidades().stream()
                            .map(esp -> new EspecialidadDTO(esp.getId(), esp.getNombre(), esp.getDescripcion()))
                            .sorted(Comparator.comparing(EspecialidadDTO::getId))
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        }

        return especialidadRepository.findAll().stream()
                .map(esp -> new EspecialidadDTO(esp.getId(), esp.getNombre(), esp.getDescripcion()))
                .sorted(Comparator.comparing(EspecialidadDTO::getId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EspecialidadResponseDTO crearEspecialidad(EspecialidadRequestDTO request) {
        if (request == null || request.getNombre() == null || request.getNombre().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad médica es obligatorio.");
        }

        String nombreNormalizado = request.getNombre().trim();

        if (especialidadRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new ConflictoException("Ya existe una especialidad médica registrada con el nombre: " + nombreNormalizado);
        }

        Especialidad nuevaEspecialidad = new Especialidad();
        nuevaEspecialidad.setNombre(nombreNormalizado);
        nuevaEspecialidad.setDescripcion(request.getDescripcion() != null && !request.getDescripcion().trim().isBlank()
                ? request.getDescripcion().trim()
                : null);

        Especialidad guardada = especialidadRepository.save(nuevaEspecialidad);
        return EspecialidadResponseDTO.fromEntity(guardada);
    }

    @Override
    @Transactional
    public EspecialidadResponseDTO actualizarEspecialidad(Long id, EspecialidadRequestDTO request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la especialidad médica es obligatorio.");
        }
        if (request == null || request.getNombre() == null || request.getNombre().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad médica es obligatorio.");
        }

        Especialidad especialidadExistente = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la especialidad médica con ID: " + id));

        String nuevoNombreNormalizado = request.getNombre().trim();

        if (especialidadRepository.existsByNombreIgnoreCaseAndIdNot(nuevoNombreNormalizado, id)) {
            throw new ConflictoException("Ya existe otra especialidad médica registrada con el nombre: " + nuevoNombreNormalizado);
        }

        especialidadExistente.setNombre(nuevoNombreNormalizado);
        especialidadExistente.setDescripcion(request.getDescripcion() != null && !request.getDescripcion().trim().isBlank()
                ? request.getDescripcion().trim()
                : null);

        Especialidad actualizada = especialidadRepository.save(especialidadExistente);
        return EspecialidadResponseDTO.fromEntity(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadResponseDTO obtenerEspecialidadPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la especialidad médica es obligatorio.");
        }

        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la especialidad médica con ID: " + id));

        return EspecialidadResponseDTO.fromEntity(especialidad);
    }
}