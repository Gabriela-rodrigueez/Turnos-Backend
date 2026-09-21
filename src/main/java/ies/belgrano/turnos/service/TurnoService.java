package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface TurnoService {
    List<TurnoResponseDTO> consultarDisponibilidad(Long especialidadId, Long profesionalId, Long sedeId, LocalDate fecha);
    TurnoResponseDTO reservarTurno(ReservaTurnoRequestDTO request);
    TurnoResponseDTO cancelarTurno(Long id);
}
