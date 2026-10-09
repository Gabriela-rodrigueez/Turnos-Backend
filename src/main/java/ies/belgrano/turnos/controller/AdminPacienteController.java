package ies.belgrano.turnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.PacienteResponseDTO;
import ies.belgrano.turnos.security.annotation.EsAdministrador;
import ies.belgrano.turnos.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/pacientes")
@EsAdministrador
@Tag(name = "Administración - Pacientes", description = "Endpoints de gestión administrativa hospitalaria para consulta y búsqueda de pacientes")
@SecurityRequirement(name = "bearerAuth")
public class AdminPacienteController {

    private final PacienteService pacienteService;

    public AdminPacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/buscar")
    @EsAdministrador
    @Operation(
            summary = "Buscar paciente por Cuil",
            description = "Permite al personal administrativo buscar un paciente por su número de CUIL. Retorna 200 OK con el DTO del paciente o 404 Not Found si no está registrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "CUIL faltante o inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado con el CUIL especificado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<PacienteResponseDTO> buscarPorCuil(
            @Parameter(description = "Número de CUIL del paciente a buscar", required = true, example = "27351112229")
            @RequestParam("cuil") String cuil
    ) {
        PacienteResponseDTO paciente = pacienteService.buscarPorCuil(cuil);
        return ResponseEntity.ok(paciente);
    }

    @GetMapping
    @EsAdministrador
    @Operation(
            summary = "Listar pacientes registrados",
            description = "Lista todos los pacientes registrados en el sistema hospitalario con soporte para filtrado por apellido o nombre."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pacientes obtenido exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PacienteResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientes(
            @Parameter(description = "Criterio de búsqueda por apellido o nombre (opcional)", example = "González")
            @RequestParam(value = "filtro", required = false) String filtro,
            @Parameter(description = "Alias alternativo para búsqueda por apellido o nombre (opcional)", example = "Juan")
            @RequestParam(value = "buscar", required = false) String buscar
    ) {
        String criterio = (filtro != null && !filtro.isBlank()) ? filtro : buscar;
        List<PacienteResponseDTO> pacientes = pacienteService.listarPacientes(criterio);
        return ResponseEntity.ok(pacientes);
    }
}
