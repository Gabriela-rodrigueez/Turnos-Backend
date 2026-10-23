package ies.belgrano.turnos.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.GeneracionSlotsResponseDTO;
import ies.belgrano.turnos.dto.GenerarSlotsRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.security.annotation.EsMedicoOAdministrador;
import ies.belgrano.turnos.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/agenda")
@Tag(name = "Agenda Médica", description = "Endpoints para la gestión masiva de slots de disponibilidad y control de cupos médicos")
@SecurityRequirement(name = "bearerAuth")
public class AgendaController {

    private final TurnoService turnoService;

    public AgendaController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping
    @EsMedicoOAdministrador
    @Operation(
            summary = "Consultar turnos y slots de la agenda médica",
            description = "Permite a médicos y administradores consultar la agenda con todos sus estados (DISPONIBLE, RESERVADO, ASISTIDO, CANCELADO). Si el usuario es médico, solo puede consultar su propia agenda."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta de agenda exitosa",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TurnoResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Requiere token JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: El usuario no posee rol MEDICO ni ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<TurnoResponseDTO>> consultarAgenda(
            @Parameter(description = "ID opcional de la especialidad") @RequestParam(required = false) Long especialidadId,
            @Parameter(description = "ID opcional del profesional") @RequestParam(required = false) Long profesionalId,
            @Parameter(description = "ID opcional de la sede hospitalaria") @RequestParam(required = false) Long sedeId,
            @Parameter(description = "Fecha opcional de consulta (Formato: YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "Estado opcional del turno (DISPONIBLE, RESERVADO, ASISTIDO, CANCELADO)")
            @RequestParam(required = false) EstadoTurno estado
    ) {
        List<TurnoResponseDTO> agenda = turnoService.consultarAgenda(especialidadId, profesionalId, sedeId, fecha, estado);
        return ResponseEntity.ok(agenda);
    }

    @PostMapping("/slots/generar")
    @EsMedicoOAdministrador
    @Operation(
            summary = "Generación masiva de slots de agenda médica",
            description = "Permite a médicos y administradores generar slots de turnos contiguos en estado DISPONIBLE en un rango de fechas, horas y días de semana."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slots generados exitosamente",
                    content = @Content(schema = @Schema(implementation = GeneracionSlotsResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o parámetros faltantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Requiere token JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: El usuario no tiene rol MEDICO ni ADMINISTRADOR, o no es el dueño de la agenda",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profesional, Especialidad o Sede no encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de solapamiento: Ya existen turnos activos o slots asignados en el rango solicitado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<GeneracionSlotsResponseDTO> generarSlots(
            @Valid @RequestBody GenerarSlotsRequestDTO request
    ) {
        GeneracionSlotsResponseDTO response = turnoService.generarSlots(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/slots/{id}")
    @EsMedicoOAdministrador
    @Operation(
            summary = "Eliminar slot de agenda",
            description = "Permite eliminar un slot siempre que se encuentre en estado DISPONIBLE. Si está RESERVADO o pertenece a otro médico, se deniega la acción."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Slot eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado: Requiere token JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Rol no autorizado o el slot pertenece a otro profesional",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: No se puede eliminar un slot que se encuentra en estado RESERVADO",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> eliminarSlot(
            @Parameter(description = "Identificador único del slot de turno a eliminar", required = true)
            @PathVariable Long id
    ) {
        turnoService.eliminarSlot(id);
        return ResponseEntity.noContent().build();
    }
}

