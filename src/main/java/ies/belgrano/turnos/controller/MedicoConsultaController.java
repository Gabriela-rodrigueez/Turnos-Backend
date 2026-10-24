package ies.belgrano.turnos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.HistoriaClinicaResponseDTO;
import ies.belgrano.turnos.dto.RegistrarAtencionRequestDTO;
import ies.belgrano.turnos.security.annotation.EsMedico;
import ies.belgrano.turnos.service.HistoriaClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/medico/consultas")
@Tag(name = "Atención Médica", description = "Endpoints para el registro de atención clínica en consultorio por profesionales médicos")
public class MedicoConsultaController {

    private final HistoriaClinicaService historiaClinicaService;

    public MedicoConsultaController(HistoriaClinicaService historiaClinicaService) {
        this.historiaClinicaService = historiaClinicaService;
    }

    @Operation(summary = "Registrar atención médica de un turno",
            description = "Permite al profesional médico registrar la evolución clínica, diagnóstico, recetas y pedidos de estudios para un turno reservado asignado a su matrícula, cambiando el turno a ASISTIDO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Atención médica registrada exitosamente",
                    content = @Content(schema = @Schema(implementation = HistoriaClinicaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de atención inválidos o turno no apto para atención",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token JWT inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Usuario no es médico o el turno pertenece a otro profesional",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @EsMedico
    @PostMapping("/{turnoId}/atender")
    public ResponseEntity<HistoriaClinicaResponseDTO> atenderTurno(
            @Parameter(description = "ID del turno a atender", example = "2")
            @PathVariable Long turnoId,
            @Valid @RequestBody RegistrarAtencionRequestDTO request) {
        HistoriaClinicaResponseDTO response = historiaClinicaService.registrarAtencion(turnoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
