package ies.belgrano.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ies.belgrano.turnos.dto.ReservaPresencialRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class TurnoServicePresencialTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfesionalRepository profesionalRepository;

    @Mock
    private EspecialidadRepository especialidadRepository;

    @Mock
    private SedeRepository sedeRepository;

    @InjectMocks
    private TurnoServiceImpl turnoService;

    @Test
    @DisplayName("reservarTurnoPresencial: Reserva exitosa retorna TurnoResponseDTO con estado RESERVADO")
    void testReservarTurnoPresencial_Exitoso() {
        Long pacienteId = 1L;
        Long profesionalId = 2L;
        Long especialidadId = 3L;
        Long sedeId = 4L;
        Long turnoId = 10L;
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2);

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                turnoId, pacienteId, "Consulta médica general", "Turno administrativo presencial"
        );

        Paciente paciente = new Paciente(pacienteId, "Juan", "González", "35111222", "juan@gmail.com", "2614001111", LocalDate.of(1990, 5, 15), null);
        Profesional profesional = new Profesional(profesionalId, "Roberto", "Pérez", "20999888", "M-12345", "roberto@hospital.com", "261555666");
        Especialidad especialidad = new Especialidad("Cardiología", "Corazón");
        especialidad.setId(especialidadId);
        Sede sede = new Sede("Hospital Central", "Alem 450", "2614490000", "Mendoza");
        sede.setId(sedeId);

        Turno turnoDisponible = new Turno(turnoId, fechaHora, fechaHora.plusMinutes(30), EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null);

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(turnoDisponible));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnoResponseDTO response = turnoService.reservarTurnoPresencial(request);

        assertNotNull(response);
        assertEquals(turnoId, response.getId());
        assertEquals(EstadoTurno.RESERVADO, response.getEstado());
        assertEquals(pacienteId, response.getPacienteId());
        assertEquals(profesionalId, response.getProfesionalId());
        assertEquals(especialidadId, response.getEspecialidadId());
        assertEquals(sedeId, response.getSedeId());
        assertEquals("Consulta médica general", response.getMotivoConsulta());
        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    @DisplayName("reservarTurnoPresencial: Lanza ConflictoException cuando el turno no se encuentra en estado DISPONIBLE")
    void testReservarTurnoPresencial_ConflictoHorario() {
        Long pacienteId = 1L;
        Long turnoId = 10L;
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2);

        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                turnoId, pacienteId, "Consulta", "Obs"
        );

        Turno turnoOcupado = new Turno(turnoId, fechaHora, fechaHora.plusMinutes(30), EstadoTurno.RESERVADO, new Paciente(), new Profesional(), new Especialidad(), new Sede(), "Consulta previa");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(new Paciente()));
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(turnoOcupado));

        assertThrows(ConflictoException.class, () -> turnoService.reservarTurnoPresencial(request));
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    @DisplayName("reservarTurnoPresencial: Lanza RecursoNoEncontradoException cuando el paciente no existe")
    void testReservarTurnoPresencial_PacienteInexistente() {
        Long pacienteIdInexistente = 999L;
        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                10L, pacienteIdInexistente, "Motivo", "Obs"
        );

        when(pacienteRepository.findById(pacienteIdInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> turnoService.reservarTurnoPresencial(request));
    }

    @Test
    @DisplayName("reservarTurnoPresencial: Lanza RecursoNoEncontradoException cuando el slot no existe")
    void testReservarTurnoPresencial_SlotInexistente() {
        Long turnoIdInexistente = 888L;
        Long pacienteId = 1L;
        ReservaPresencialRequestDTO request = new ReservaPresencialRequestDTO(
                turnoIdInexistente, pacienteId, "Motivo", "Obs"
        );

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(new Paciente()));
        when(turnoRepository.findById(turnoIdInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> turnoService.reservarTurnoPresencial(request));
    }
}
