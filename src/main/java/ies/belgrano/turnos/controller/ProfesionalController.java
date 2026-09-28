package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.dto.ProfesionalDTO;
import ies.belgrano.turnos.service.ProfesionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profesionales")
@Tag(name = "Catálogo de Profesionales", description = "Endpoints para consultar y filtrar profesionales médicos")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @GetMapping
    @Operation(summary = "Listar profesionales con filtros opcionales", 
               description = "Retorna profesionales filtrados por especialidadId y/o sedeId. Si no hay resultados, retorna 204.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No se encontraron profesionales con esos criterios")
    })
    public ResponseEntity<List<ProfesionalDTO>> listarProfesionales(
            @Parameter(description = "ID opcional de la especialidad para filtrar") 
            @RequestParam(required = false) Long especialidadId,
            
            @Parameter(description = "ID opcional de la sede para filtrar") 
            @RequestParam(required = false) Long sedeId) {
        
        // Llamamos al servicio pasando los parámetros de filtro opcionales
        List<ProfesionalDTO> profesionales = profesionalService.listarProfesionales(especialidadId, sedeId);

        // Si la lista está vacía, devolvemos 204 No Content 
        if (profesionales.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(profesionales);
    }
}