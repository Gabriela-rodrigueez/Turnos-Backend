package ies.belgrano.turnos.controller;

import ies.belgrano.turnos.dto.SedeDTO;
import ies.belgrano.turnos.service.SedeService;
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
@RequestMapping("/api/v1/sedes")
// @Tag: Es para Swagger. Le da un título y descripción bonitos en la página de documentación.
@Tag(name = "Catálogo de Sedes", description = "Endpoints para consultar las sedes hospitalarias")
public class SedeController {

    // Declaramos una variable. Usamos "final" para que no pueda ser modificada.
    private final SedeService sedeService;

    // Inyección de dependencias por constructor
    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @GetMapping
    // @Operation y @ApiResponses: Son anotaciones de Swagger para documentar qué hace el endpoint 
    @Operation(summary = "Listar todas las sedes", description = "Retorna una lista de todas las sedes hospitalarias.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No se encontraron sedes (Cuerpo vacío)")
    })
    public ResponseEntity<List<SedeDTO>> listarSedes() {
        
        // 1. Llamamos a nuestro servicio para que nos traiga la lista de sedes.
        List<SedeDTO> sedes = sedeService.listarSedes();

        // Si la lista está vacía,la respuesta es el código 204 y sin cuerpo.
        if (sedes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // 3. Si la lista tiene elementos, respondemos con un código 200 (ok) 
        return ResponseEntity.ok(sedes);
    }
}