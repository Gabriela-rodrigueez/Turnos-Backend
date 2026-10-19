package ies.belgrano.turnos.dto;

import ies.belgrano.turnos.model.EstadoTurno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de solicitud para modificar el ciclo de vida / estado de un turno médico")
public class CambiarEstadoTurnoRequestDTO {

    @NotNull(message = "El nuevo estado del turno es obligatorio")
    @Schema(description = "Nuevo estado del turno (ASISTIDO, CANCELADO, RESERVADO)", example = "ASISTIDO", requiredMode = Schema.RequiredMode.REQUIRED)
    private EstadoTurno estado;

    @Schema(description = "Observaciones o justificación del cambio de estado", example = "Consulta realizada y prescripción médica emitida")
    private String observaciones;

    public CambiarEstadoTurnoRequestDTO() {
    }

    public CambiarEstadoTurnoRequestDTO(EstadoTurno estado) {
        this.estado = estado;
    }

    public CambiarEstadoTurnoRequestDTO(EstadoTurno estado, String observaciones) {
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
