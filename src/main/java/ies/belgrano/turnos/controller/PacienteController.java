package ies.belgrano.turnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.security.annotation.EsPacienteOAdministrador;
import ies.belgrano.turnos.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/pacientes")
@Tag(name = "Pacientes", description = "API REST para gestión de pacientes y consulta de sus turnos")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/{pacienteId}/turnos")
    @EsPacienteOAdministrador
    @Operation(
            summary = "Consultar turnos de un paciente",
            description = "Retorna el listado de turnos de un paciente por su ID. Si no posee turnos agendados, retorna 204 No Content. Si el paciente no existe, retorna 404 Not Found."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turnos obtenidos con éxito",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TurnoResponseDTO.class)))),
            @ApiResponse(responseCode = "204", description = "El paciente existe pero no posee turnos agendados (Cuerpo vacío)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontró el paciente con el ID especificado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<TurnoResponseDTO>> obtenerTurnosPorPaciente(
            @Parameter(description = "ID del paciente a consultar", required = true)
            @PathVariable Long pacienteId
    ) {
        List<TurnoResponseDTO> turnos = pacienteService.obtenerTurnosPorPaciente(pacienteId);

        if (turnos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(turnos);
    }
}