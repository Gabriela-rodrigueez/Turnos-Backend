package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/turnos")
@Tag(name = "Turnos", description = "API REST para gestión de disponibilidad, reserva y cancelación de turnos hospitalarios")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Consultar disponibilidad de turnos", description = "Retorna un listado de turnos filtrados por especialidad, profesional, sede y/o fecha.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada con éxito",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TurnoResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<TurnoResponseDTO>> consultarDisponibilidad(
            @Parameter(description = "ID opcional de la especialidad") @RequestParam(required = false) Long especialidadId,
            @Parameter(description = "ID opcional del profesional") @RequestParam(required = false) Long profesionalId,
            @Parameter(description = "ID opcional de la sede hospitalaria") @RequestParam(required = false) Long sedeId,
            @Parameter(description = "Fecha opcional de consulta (Formato: YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        List<TurnoResponseDTO> disponibilidades = turnoService.consultarDisponibilidad(especialidadId, profesionalId, sedeId, fecha);
        return ResponseEntity.ok(disponibilidades);
    }

    @PostMapping("/reserva")
    @Operation(summary = "Crear reserva de turno", description = "Permite a un paciente reservar un turno con un profesional, especialidad y sede en una fecha/hora determinada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turno reservado exitosamente",
                    content = @Content(schema = @Schema(implementation = TurnoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paciente, Profesional, Especialidad o Sede no encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario: El profesional ya tiene un turno reservado en ese mismo horario",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<TurnoResponseDTO> reservarTurno(@Valid @RequestBody ReservaTurnoRequestDTO request) {
        TurnoResponseDTO turnoReservado = turnoService.reservarTurno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoReservado);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva de turno", description = "Cambia el estado de un turno activo a CANCELADO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno cancelado exitosamente",
                    content = @Content(schema = @Schema(implementation = TurnoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el turno especificado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<TurnoResponseDTO> cancelarTurno(
            @Parameter(description = "ID del turno a cancelar", required = true) @PathVariable Long id
    ) {
        TurnoResponseDTO turnoCancelado = turnoService.cancelarTurno(id);
        return ResponseEntity.ok(turnoCancelado);
    }
}
