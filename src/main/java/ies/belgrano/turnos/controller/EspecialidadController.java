package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.service.EspecialidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especialidades")
@Tag(name = "Catálogo de Especialidades", description = "Endpoints para consultar las especialidades médicas")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las especialidades", description = "Retorna una lista de todas las especialidades. Si no hay registros, retorna 204 No Content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No se encontraron especialidades (Cuerpo vacío)")
    })
    public ResponseEntity<List<EspecialidadDTO>> listarEspecialidades() {
        List<EspecialidadDTO> especialidades = especialidadService.listarEspecialidades();

        if (especialidades.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(especialidades);
    }
}