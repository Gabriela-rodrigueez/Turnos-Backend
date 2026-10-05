package ies.belgrano.turnos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.ReservaPresencialRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.security.annotation.EsAdministrador;
import ies.belgrano.turnos.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/turnos")
@EsAdministrador
@Tag(name = "Administración - Turnos", description = "Endpoints de gestión administrativa hospitalaria para agendamiento presencial de turnos")
@SecurityRequirement(name = "bearerAuth")
public class AdminTurnoController {

    private final TurnoService turnoService;

    public AdminTurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping("/reserva-presencial")
    @EsAdministrador
    @Operation(
            summary = "Agendar turno presencial administrativo",
            description = "Permite al personal administrativo agendar un turno médico presencial indicando pacienteId, profesionalId, especialidadId, sedeId y fechaHora."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turno presencial agendado exitosamente",
                    content = @Content(schema = @Schema(implementation = TurnoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltantes (ej. campos nulos o fecha en el pasado)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paciente, Profesional, Especialidad o Sede no encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario: El profesional ya tiene un turno reservado en ese mismo horario",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<TurnoResponseDTO> reservarTurnoPresencial(
            @Valid @RequestBody ReservaPresencialRequestDTO request
    ) {
        TurnoResponseDTO turnoReservado = turnoService.reservarTurnoPresencial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoReservado);
    }
}
