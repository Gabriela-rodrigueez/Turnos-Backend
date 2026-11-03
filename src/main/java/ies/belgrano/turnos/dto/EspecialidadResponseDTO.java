package ies.belgrano.turnos.dto;

import ies.belgrano.turnos.model.Especialidad;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de respuesta con el detalle de una especialidad médica hospitalaria")
public class EspecialidadResponseDTO extends EspecialidadDTO {

    public EspecialidadResponseDTO() {
        super();
    }

    public EspecialidadResponseDTO(Long id, String nombre, String descripcion) {
        super(id, nombre, descripcion);
    }

    public static EspecialidadResponseDTO fromEntity(Especialidad especialidad) {
        if (especialidad == null) {
            return null;
        }
        return new EspecialidadResponseDTO(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion()
        );
    }
}

