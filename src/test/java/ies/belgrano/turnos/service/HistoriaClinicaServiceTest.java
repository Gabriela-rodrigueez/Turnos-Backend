package ies.belgrano.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import ies.belgrano.turnos.dto.HistoriaClinicaResponseDTO;
import ies.belgrano.turnos.dto.RegistrarAtencionRequestDTO;
import ies.belgrano.turnos.exception.AccesoDenegadoException;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.EstadoTurno;
import ies.belgrano.turnos.model.HistoriaClinica;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Sede;
import ies.belgrano.turnos.model.Turno;
import ies.belgrano.turnos.repository.HistoriaClinicaRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.TurnoRepository;
import ies.belgrano.turnos.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class HistoriaClinicaServiceTest {

    @Mock
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private HistoriaClinicaServiceImpl historiaClinicaService;

    private Paciente paciente;
    private Profesional profesional1;
    private Profesional profesional2;
    private Turno turno;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Juan");
        paciente.setApellido("González");
        paciente.setDni("35111222");

        profesional1 = new Profesional();
        profesional1.setId(10L);
        profesional1.setNombre("Roberto");
        profesional1.setApellido("Pérez");
        profesional1.setMatricula("M-100");

        profesional2 = new Profesional();
        profesional2.setId(20L);
        profesional2.setNombre("María");
        profesional2.setApellido("Gómez");
        profesional2.setMatricula("M-200");

        Especialidad esp = new Especialidad();
        esp.setId(1L);
        esp.setNombre("Cardiología");

        Sede sede = new Sede();
        sede.setId(1L);
        sede.setNombre("Hospital Central");

        turno = new Turno();
        turno.setId(100L);
        turno.setPaciente(paciente);
        turno.setProfesional(profesional1);
        turno.setEspecialidad(esp);
        turno.setSede(sede);
        turno.setEstado(EstadoTurno.RESERVADO);
        turno.setFechaHoraInicio(LocalDateTime.now());
        turno.setFechaHoraFin(LocalDateTime.now().plusMinutes(30));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void simularUsuario(Long usuarioId, Rol rol, Long pacienteId, Long profesionalId) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "test@test.com", rol, pacienteId, profesionalId);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Debe registrar la atención médica exitosamente cuando el médico asignado atiende su turno")
    void registrarAtencion_Exito() {
        simularUsuario(1L, Rol.MEDICO, null, 10L);

        RegistrarAtencionRequestDTO request = new RegistrarAtencionRequestDTO(
                "Dolor en el pecho",
                "Hipertensión leve",
                "Dieta y control",
                "Enalapril 10mg",
                "ECG"
        );

        when(turnoRepository.findById(100L)).thenReturn(Optional.of(turno));
        when(historiaClinicaRepository.save(any(HistoriaClinica.class))).thenAnswer(inv -> {
            HistoriaClinica h = inv.getArgument(0);
            h.setId(500L);
            return h;
        });

        HistoriaClinicaResponseDTO res = historiaClinicaService.registrarAtencion(100L, request);

        assertNotNull(res);
        assertEquals(500L, res.getId());
        assertEquals("Hipertensión leve", res.getDiagnostico());
        assertEquals(EstadoTurno.ASISTIDO, turno.getEstado());
        verify(turnoRepository).save(turno);
        verify(historiaClinicaRepository).save(any(HistoriaClinica.class));
    }

    @Test
    @DisplayName("Debe lanzar AccesoDenegadoException si un médico intenta atender un turno de otro profesional")
    void registrarAtencion_MedicoDiferente_LanzaAccesoDenegado() {
        simularUsuario(2L, Rol.MEDICO, null, 20L); // Profesional 20 intenta atender turno de Profesional 10

        RegistrarAtencionRequestDTO request = new RegistrarAtencionRequestDTO("Motivo", "Diagnóstico", null, null, null);
        when(turnoRepository.findById(100L)).thenReturn(Optional.of(turno));

        assertThrows(AccesoDenegadoException.class, () ->
                historiaClinicaService.registrarAtencion(100L, request)
        );
        verify(historiaClinicaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe permitir al paciente consultar su propio historial clínico")
    void obtenerHistorialPorPaciente_PacienteMismoId_Exito() {
        simularUsuario(1L, Rol.PACIENTE, 1L, null);

        HistoriaClinica h = new HistoriaClinica(paciente, profesional1, turno, "Motivo", "Diagnóstico", "Tratamiento", null, null);
        h.setId(1L);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(historiaClinicaRepository.findByPacienteIdOrderByFechaAtencionDesc(1L)).thenReturn(List.of(h));

        List<HistoriaClinicaResponseDTO> resultado = historiaClinicaService.obtenerHistorialPorPaciente(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Juan González", resultado.get(0).getPacienteNombreCompleto());
    }

    @Test
    @DisplayName("Debe lanzar AccesoDenegadoException si un paciente intenta consultar el historial de otro paciente")
    void obtenerHistorialPorPaciente_PacienteOtroId_LanzaAccesoDenegado() {
        simularUsuario(1L, Rol.PACIENTE, 1L, null); // Paciente 1 intenta consultar al Paciente 2

        assertThrows(AccesoDenegadoException.class, () ->
                historiaClinicaService.obtenerHistorialPorPaciente(2L)
        );
    }

    @Test
    @DisplayName("Debe permitir a cualquier médico consultar el historial de un paciente")
    void obtenerHistorialPorPaciente_MedicoConsultaCualquierPaciente_Exito() {
        simularUsuario(99L, Rol.MEDICO, null, 10L); // Médico consulta paciente 1

        HistoriaClinica h = new HistoriaClinica(paciente, profesional1, turno, "Motivo", "Diagnóstico", null, null, null);
        h.setId(2L);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(historiaClinicaRepository.findByPacienteIdOrderByFechaAtencionDesc(1L)).thenReturn(List.of(h));

        List<HistoriaClinicaResponseDTO> resultado = historiaClinicaService.obtenerHistorialPorPaciente(1L);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getId());
    }

    @Test
    @DisplayName("Debe retornar mi historial usando pacienteId de la sesión")
    void obtenerMiHistorial_Exito() {
        simularUsuario(1L, Rol.PACIENTE, 1L, null);

        HistoriaClinica h = new HistoriaClinica(paciente, profesional1, turno, "Motivo", "Diagnóstico", null, null, null);
        h.setId(1L);

        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(historiaClinicaRepository.findByPacienteIdOrderByFechaAtencionDesc(1L)).thenReturn(List.of(h));

        List<HistoriaClinicaResponseDTO> res = historiaClinicaService.obtenerMiHistorial();
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Debe validar ownership al consultar un registro clínico por ID si el usuario es paciente")
    void obtenerRegistroPorId_PacienteDiferente_LanzaAccesoDenegado() {
        simularUsuario(2L, Rol.PACIENTE, 2L, null); // Paciente 2 intenta consultar registro del paciente 1

        HistoriaClinica h = new HistoriaClinica(paciente, profesional1, turno, "Motivo", "Diagnóstico", null, null, null);
        h.setId(50L);

        when(historiaClinicaRepository.findById(50L)).thenReturn(Optional.of(h));

        assertThrows(AccesoDenegadoException.class, () ->
                historiaClinicaService.obtenerRegistroPorId(50L)
        );
    }
}
