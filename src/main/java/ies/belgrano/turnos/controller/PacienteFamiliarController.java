package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.dto.FamiliarRequestDTO;
import ies.belgrano.turnos.dto.PacienteResponseDTO;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteFamiliarController {

    private final PacienteService pacienteService;

    public PacienteFamiliarController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    // 1. Registrar dependiente / familiar
    @PostMapping("/{tutorId}/familiares")
    public ResponseEntity<PacienteResponseDTO> registrarFamiliar(
            @PathVariable Long tutorId,
            @Valid @RequestBody FamiliarRequestDTO familiarDto) {
        
        // Mapeo del DTO de entrada a la entidad Paciente
        Paciente pacienteFamiliar = new Paciente();
        pacienteFamiliar.setNombre(familiarDto.getNombre());
        pacienteFamiliar.setApellido(familiarDto.getApellido());
        pacienteFamiliar.setDni(familiarDto.getDni());
        pacienteFamiliar.setFechaNacimiento(familiarDto.getFechaNacimiento());
        pacienteFamiliar.setTelefono(familiarDto.getTelefono());
        pacienteFamiliar.setParentesco(familiarDto.getParentesco());
        
        // Si cuentas con un servicio/repositorio de ObraSocial, aquí se asignaría buscando por el ID.
        // pacienteFamiliar.setObraSocial(...); 

        PacienteResponseDTO nuevoFamiliar = pacienteService.registrarFamiliar(tutorId, pacienteFamiliar);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoFamiliar);
    }

    // 2. Listar familiares activos del tutor
    @GetMapping("/{tutorId}/familiares")
    public ResponseEntity<List<PacienteResponseDTO>> listarFamiliaresActivos(@PathVariable Long tutorId) {
        List<PacienteResponseDTO> familiares = pacienteService.listarFamiliaresActivos(tutorId);
        return ResponseEntity.ok(familiares);
    }

    // 3. Baja lógica de un familiar
    @DeleteMapping("/{tutorId}/familiares/{familiarId}")
    public ResponseEntity<Void> bajaLogicaFamiliar(
            @PathVariable Long tutorId,
            @PathVariable Long familiarId) {
        pacienteService.bajaLogicaFamiliar(tutorId, familiarId);
        return ResponseEntity.noContent().build();
    }

    // 4. Reactivar un familiar
    @PatchMapping("/{tutorId}/familiares/{familiarId}/reactivar")
    public ResponseEntity<Void> reactivarFamiliar(
            @PathVariable Long tutorId,
            @PathVariable Long familiarId) {
        pacienteService.reactivarFamiliar(tutorId, familiarId);
        return ResponseEntity.ok().build();
    }
}