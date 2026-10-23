package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.CambiarEstadoTurnoRequestDTO;
import ies.belgrano.turnos.dto.GeneracionSlotsResponseDTO;
import ies.belgrano.turnos.dto.GenerarSlotsRequestDTO;
import ies.belgrano.turnos.dto.ReservaPresencialRequestDTO;
import ies.belgrano.turnos.dto.ReservaTurnoRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;

import ies.belgrano.turnos.model.EstadoTurno;

import java.time.LocalDate;
import java.util.List;

public interface TurnoService {
    GeneracionSlotsResponseDTO generarSlots(GenerarSlotsRequestDTO request);
    void eliminarSlot(Long id);
    TurnoResponseDTO cambiarEstado(Long id, CambiarEstadoTurnoRequestDTO request);
    List<TurnoResponseDTO> consultarDisponibilidad(Long especialidadId, Long profesionalId, Long sedeId, LocalDate fecha);
    List<TurnoResponseDTO> consultarAgenda(Long especialidadId, Long profesionalId, Long sedeId, LocalDate fecha, EstadoTurno estado);
    TurnoResponseDTO reservarTurno(ReservaTurnoRequestDTO request);
    TurnoResponseDTO reservarTurnoPresencial(ReservaPresencialRequestDTO request);
    TurnoResponseDTO cancelarTurno(Long id);
}
