package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.dto.ErrorResponseDTO;
import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.dto.EspecialidadRequestDTO;
import ies.belgrano.turnos.dto.EspecialidadResponseDTO;
import ies.belgrano.turnos.security.annotation.EsAdministrador;
import ies.belgrano.turnos.service.EspecialidadService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/especialidades")
@EsAdministrador
@Tag(name = "Administración - Especialidades", description = "Endpoints de administración hospitalaria para el alta y gestión de especialidades médicas (Issue B-12)")
@SecurityRequirement(name = "bearerAuth")
public class AdminEspecialidadController {

    private final EspecialidadService especialidadService;

    public AdminEspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @PostMapping
    @EsAdministrador
    @Operation(
            summary = "Alta de especialidad médica",
            description = "Permite al personal administrador dar de alta una nueva especialidad médica validando la unicidad del nombre (insensible a mayúsculas/minúsculas). Retorna 201 Created con el encabezado Location del recurso creado o 409 Conflict si ya existe."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Especialidad médica dada de alta exitosamente",
                    content = @Content(schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos o faltantes (ej. nombre en blanco o excede longitud)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: Ya existe una especialidad con el nombre especificado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<EspecialidadResponseDTO> crearEspecialidad(
            @Valid @RequestBody EspecialidadRequestDTO request
    ) {
        EspecialidadResponseDTO creada = especialidadService.crearEspecialidad(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    @EsAdministrador
    @Operation(
            summary = "Editar especialidad médica",
            description = "Permite al personal administrador modificar el nombre y/o descripción clínica de una especialidad existente. Valida la existencia (404 Not Found) y la no colisión de nombre con otras especialidades (409 Conflict)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especialidad médica actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos o faltantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada para el ID especificado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: El nombre colisiona con otra especialidad médica existente",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<EspecialidadResponseDTO> actualizarEspecialidad(
            @Parameter(description = "Identificador único de la especialidad", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadRequestDTO request
    ) {
        EspecialidadResponseDTO actualizada = especialidadService.actualizarEspecialidad(id, request);
        return ResponseEntity.ok(actualizada);
    }

    @GetMapping("/{id}")
    @EsAdministrador
    @Operation(
            summary = "Detalle de especialidad médica por ID",
            description = "Permite al personal administrador consultar el detalle completo de una especialidad médica mediante su identificador único. Retorna 200 OK o 404 Not Found."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especialidad médica encontrada exitosamente",
                    content = @Content(schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Identificador de especialidad inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada para el ID especificado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<EspecialidadResponseDTO> obtenerEspecialidadPorId(
            @Parameter(description = "Identificador único de la especialidad", required = true, example = "1")
            @PathVariable Long id
    ) {
        EspecialidadResponseDTO especialidad = especialidadService.obtenerEspecialidadPorId(id);
        return ResponseEntity.ok(especialidad);
    }

    @GetMapping
    @EsAdministrador
    @Operation(
            summary = "Listar todas las especialidades médicas para administración",
            description = "Permite al administrador obtener el listado completo de especialidades registradas en el sistema hospitalario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de especialidades obtenido exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EspecialidadDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado: Se requiere token JWT válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol ADMINISTRADOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<EspecialidadDTO>> listarEspecialidadesAdmin() {
        List<EspecialidadDTO> especialidades = especialidadService.listarEspecialidades();
        return ResponseEntity.ok(especialidades);
    }
}

