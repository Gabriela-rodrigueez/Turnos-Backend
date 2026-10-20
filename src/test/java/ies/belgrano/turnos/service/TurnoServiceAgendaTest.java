package ies.belgrano.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import ies.belgrano.turnos.dto.CambiarEstadoTurnoRequestDTO;
import ies.belgrano.turnos.dto.GeneracionSlotsResponseDTO;
import ies.belgrano.turnos.dto.GenerarSlotsRequestDTO;
import ies.belgrano.turnos.dto.TurnoResponseDTO;
import ies.belgrano.turnos.exception.AccesoDenegadoException;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.ConflictoHorarioException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.SedeRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class TurnoServiceAgendaTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private ProfesionalRepository profesionalRepository;

    @Mock
    private EspecialidadRepository especialidadRepository;

    @Mock
    private SedeRepository sedeRepository;

    @InjectMocks
    private TurnoServiceImpl turnoService;

    private Profesional profesional;
    private Especialidad especialidad;
    private Sede sede;

    @BeforeEach
    void setUp() {
        profesional = new Profesional(1L, "Martín", "Palermo", "20111222", "M-9999", "martin@hospital.gob.ar", "2614441111");
        especialidad = new Especialidad("Traumatología", "Afecciones óseas");
        especialidad.setId(2L);
        sede = new Sede("Hospital Central", "Alem 450", "2614490000", "Mendoza");
        sede.setId(3L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("generarSlots: Genera slots correctamente cuando no hay solapamiento")
    void testGenerarSlots_Exitoso() {
        // Lunes a Martes (2 días), 08:00 a 09:00 cada 30 minutos = 2 slots por día = 4 slots
        LocalDate desde = LocalDate.of(2026, 11, 2); // Lunes
        LocalDate hasta = LocalDate.of(2026, 11, 3); // Martes

        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                desde,
                hasta,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                30,
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        );

        when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(especialidadRepository.findById(2L)).thenReturn(Optional.of(especialidad));
        when(sedeRepository.findById(3L)).thenReturn(Optional.of(sede));
        when(turnoRepository.findSolapamientos(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(EstadoTurno.CANCELADO)))
                .thenReturn(Collections.emptyList());
        when(turnoRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        GeneracionSlotsResponseDTO response = turnoService.generarSlots(request);

        assertNotNull(response);
        assertEquals(4, response.getCantidadSlotsGenerados());
        assertEquals(profesional.getId(), response.getProfesionalId());
        assertEquals("Martín Palermo", response.getProfesionalNombreCompleto());
        assertEquals(4, response.getSlots().size());
        assertEquals(EstadoTurno.DISPONIBLE, response.getSlots().get(0).getEstado());
        assertNull(response.getSlots().get(0).getPacienteId());
        verify(turnoRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("generarSlots: Lanza ConflictoHorarioException cuando se detecta un turno solapado activo")
    void testGenerarSlots_ConflictoSolapamiento() {
        LocalDate fecha = LocalDate.of(2026, 11, 2); // Lunes
        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional.getId(),
                especialidad.getId(),
                sede.getId(),
                fecha,
                fecha,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                30,
                List.of(DayOfWeek.MONDAY)
        );

        Turno turnoExistente = new Turno(
                99L,
                fecha.atTime(8, 0),
                fecha.atTime(8, 30),
                EstadoTurno.RESERVADO,
                null,
                profesional,
                especialidad,
                sede,
                null
        );

        when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(especialidadRepository.findById(2L)).thenReturn(Optional.of(especialidad));
        when(sedeRepository.findById(3L)).thenReturn(Optional.of(sede));
        when(turnoRepository.findSolapamientos(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(EstadoTurno.CANCELADO)))
                .thenReturn(List.of(turnoExistente));

        assertThrows(ConflictoHorarioException.class, () -> turnoService.generarSlots(request));
        verify(turnoRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("generarSlots: Lanza AccesoDenegadoException si un médico intenta generar slots para otro profesional")
    void testGenerarSlots_MedicoIntentaGenerarParaOtroProfesional() {
        AuthenticatedUser medicoUser = new AuthenticatedUser(10L, "otro@hospital.com", Rol.MEDICO, null, 999L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(medicoUser, null, List.of(new SimpleGrantedAuthority("MEDICO")))
        );

        GenerarSlotsRequestDTO request = new GenerarSlotsRequestDTO(
                profesional.getId(), // ID = 1L != 999L
                especialidad.getId(),
                sede.getId(),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 2),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                30,
                null
        );

        when(profesionalRepository.findById(1L)).thenReturn(Optional.of(profesional));

        assertThrows(AccesoDenegadoException.class, () -> turnoService.generarSlots(request));
    }

    @Test
    @DisplayName("eliminarSlot: Elimina con éxito un slot en estado DISPONIBLE")
    void testEliminarSlot_Exitoso() {
        Turno slot = new Turno(10L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30),
                EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null);

        when(turnoRepository.findById(10L)).thenReturn(Optional.of(slot));

        turnoService.eliminarSlot(10L);

        verify(turnoRepository).delete(slot);
    }

    @Test
    @DisplayName("eliminarSlot: Lanza ConflictoException si el slot se encuentra en estado RESERVADO")
    void testEliminarSlot_ErrorSlotReservado() {
        Turno slot = new Turno(10L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30),
                EstadoTurno.RESERVADO, null, profesional, especialidad, sede, null);

        when(turnoRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(ConflictoException.class, () -> turnoService.eliminarSlot(10L));
        verify(turnoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("eliminarSlot: Lanza AccesoDenegadoException si un médico intenta eliminar slot de otro profesional")
    void testEliminarSlot_MedicoEliminaSlotDeOtroProfesional() {
        AuthenticatedUser medicoUser = new AuthenticatedUser(10L, "otro@hospital.com", Rol.MEDICO, null, 999L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(medicoUser, null, List.of(new SimpleGrantedAuthority("MEDICO")))
        );

        Turno slot = new Turno(10L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30),
                EstadoTurno.DISPONIBLE, null, profesional, especialidad, sede, null); // profesionalId = 1L

        when(turnoRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(AccesoDenegadoException.class, () -> turnoService.eliminarSlot(10L));
        verify(turnoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("cambiarEstado: Transiciona exitosamente a ASISTIDO")
    void testCambiarEstado_Exitoso() {
        Turno turno = new Turno(20L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30),
                EstadoTurno.RESERVADO, null, profesional, especialidad, sede, null);

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.ASISTIDO, "Paciente atendido en consultorio");

        when(turnoRepository.findById(20L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnoResponseDTO response = turnoService.cambiarEstado(20L, request);

        assertNotNull(response);
        assertEquals(EstadoTurno.ASISTIDO, response.getEstado());
        verify(turnoRepository).save(turno);
    }

    @Test
    @DisplayName("cambiarEstado: Lanza ConflictoException si el estado destino no es ASISTIDO, CANCELADO o RESERVADO")
    void testCambiarEstado_EstadoInvalido() {
        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.DISPONIBLE, "Revertir a disponible");

        assertThrows(ConflictoException.class, () -> turnoService.cambiarEstado(20L, request));
        verify(turnoRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstado: Lanza AccesoDenegadoException si médico intenta modificar turno de otro profesional")
    void testCambiarEstado_MedicoModificaTurnoDeOtro() {
        AuthenticatedUser medicoUser = new AuthenticatedUser(10L, "otro@hospital.com", Rol.MEDICO, null, 999L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(medicoUser, null, List.of(new SimpleGrantedAuthority("MEDICO")))
        );

        Turno turno = new Turno(20L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusMinutes(30),
                EstadoTurno.RESERVADO, null, profesional, especialidad, sede, null); // profesionalId = 1L

        CambiarEstadoTurnoRequestDTO request = new CambiarEstadoTurnoRequestDTO(EstadoTurno.ASISTIDO, "Nota");

        when(turnoRepository.findById(20L)).thenReturn(Optional.of(turno));

        assertThrows(AccesoDenegadoException.class, () -> turnoService.cambiarEstado(20L, request));
        verify(turnoRepository, never()).save(any());
    }
}

