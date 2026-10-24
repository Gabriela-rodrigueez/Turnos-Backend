package ies.belgrano.turnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.HistoriaClinicaResponseDTO;
import ies.belgrano.turnos.security.annotation.EsPaciente;
import ies.belgrano.turnos.security.annotation.EsPacienteOMedicoOAdministrador;
import ies.belgrano.turnos.service.HistoriaClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/historial-clinico")
@Tag(name = "Historial Clínico", description = "Endpoints para la consulta segura del historial clínico de pacientes")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;

    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService) {
        this.historiaClinicaService = historiaClinicaService;
    }

    @Operation(summary = "Consultar mi historial clínico", description = "Permite al paciente autenticado obtener su lista cronológica de atenciones médicas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial clínico obtenido exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoriaClinicaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token JWT inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: El usuario no posee rol PACIENTE",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @EsPaciente
    @GetMapping("/mi-historial")
    public ResponseEntity<List<HistoriaClinicaResponseDTO>> obtenerMiHistorial() {
        return ResponseEntity.ok(historiaClinicaService.obtenerMiHistorial());
    }

    @Operation(summary = "Consultar historial de un paciente por ID",
            description = "Médicos y administradores pueden consultar el historial de cualquier paciente. Los pacientes solo pueden consultar su propio ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial clínico obtenido exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoriaClinicaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token JWT inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Paciente intentando acceder al historial de otro paciente",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @EsPacienteOMedicoOAdministrador
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistoriaClinicaResponseDTO>> obtenerHistorialPaciente(
            @Parameter(description = "ID único del paciente a consultar", example = "1")
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaClinicaService.obtenerHistorialPorPaciente(pacienteId));
    }

    @Operation(summary = "Consultar detalle de un registro clínico",
            description = "Retorna el detalle completo de una atención médica. Valida propiedad si el solicitante es paciente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro clínico obtenido",
                    content = @Content(schema = @Schema(implementation = HistoriaClinicaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado o token JWT inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a registro clínico ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Registro clínico no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @EsPacienteOMedicoOAdministrador
    @GetMapping("/{registroId}")
    public ResponseEntity<HistoriaClinicaResponseDTO> obtenerRegistroPorId(
            @Parameter(description = "ID del registro de historia clínica", example = "1")
            @PathVariable Long registroId) {
        return ResponseEntity.ok(historiaClinicaService.obtenerRegistroPorId(registroId));
    }
}
