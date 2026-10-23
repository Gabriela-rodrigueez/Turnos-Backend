package ies.belgrano.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ies.belgrano.turnos.dto.PacienteResponseDTO;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.ObraSocial;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    @Test
    @DisplayName("buscarPorDni: Retorna PacienteResponseDTO cuando el paciente existe")
    void testBuscarPorDni_Exitoso() {
        String dni = "35111222";
        ObraSocial osep = new ObraSocial("OSEP Mendoza", "OSEP-001");
        osep.setId(1L);
        Paciente paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Juan");
        paciente.setApellido("González");
        paciente.setDni(dni);
        paciente.setEmail("juan@gmail.com");
        paciente.setTelefono("2614001111");
        paciente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        paciente.setObraSocial(osep);
        when(pacienteRepository.findByDni(dni)).thenReturn(Optional.of(paciente));

        PacienteResponseDTO resultado = pacienteService.buscarPorDni(dni);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("González", resultado.getApellido());
        assertEquals(dni, resultado.getDni());
        assertEquals("OSEP Mendoza", resultado.getObraSocialNombre());
        verify(pacienteRepository).findByDni(dni);
    }

    @Test
    @DisplayName("buscarPorDni: Lanza RecursoNoEncontradoException cuando el paciente no existe")
    void testBuscarPorDni_NoExiste_LanzaExcepcion() {
        String dni = "35111222";
        when(pacienteRepository.findByDni(dni)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () -> {
            pacienteService.buscarPorDni(dni);
        });

        assertEquals("No se encontró ningún paciente con el DNI: " + dni, ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorDni: Lanza IllegalArgumentException si el DNI es nulo o vacío")
    void testBuscarPorDni_DniInvalido_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> pacienteService.buscarPorDni(null));
        assertThrows(IllegalArgumentException.class, () -> pacienteService.buscarPorDni("   "));
    }

    @Test
    @DisplayName("listarPacientes: Retorna todos los pacientes cuando el filtro es nulo o vacío")
    void testListarPacientes_SinFiltro() {
        Paciente p1 = new Paciente();
        p1.setId(1L);
        p1.setNombre("Juan");
        p1.setApellido("González");
        p1.setDni("35111222");

        Paciente p2 = new Paciente();
        p2.setId(2L);
        p2.setNombre("Lucía");
        p2.setApellido("Martínez");
        p2.setDni("40222333");
        
        when(pacienteRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PacienteResponseDTO> resultado = pacienteService.listarPacientes(null);

        assertEquals(2, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        assertEquals("Lucía", resultado.get(1).getNombre());
        verify(pacienteRepository).findAll();
    }

    @Test
    @DisplayName("listarPacientes: Retorna pacientes filtrados cuando se proporciona un criterio de búsqueda")
    void testListarPacientes_ConFiltro() {
        String filtro = "González";
        Paciente p1 = new Paciente();
        p1.setId(1L);
        p1.setNombre("Juan");
        p1.setApellido("González");
        p1.setDni("35111222");

        Paciente p2 = new Paciente();
        p2.setId(2L);
        p2.setNombre("Lucía");
        p2.setApellido("Martínez");
        p2.setDni("40222333");
        when(pacienteRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PacienteResponseDTO> resultado = pacienteService.listarPacientes(filtro);

        assertEquals(1, resultado.size());
        assertEquals("González", resultado.get(0).getApellido());
        verify(pacienteRepository).findAll();
    }

    @Test
    @DisplayName("listarPacientes: Búsqueda insensible a tildes y mayúsculas")
    void testListarPacientes_InsensibleTildes() {
        Paciente p1 = new Paciente();
        p1.setId(1L);
        p1.setNombre("Lucía");
        p1.setApellido("Martínez");
        p1.setDni("40222333");
        when(pacienteRepository.findAll()).thenReturn(List.of(p1));

        List<PacienteResponseDTO> resultado = pacienteService.listarPacientes("lucia");

        assertEquals(1, resultado.size());
        assertEquals("Lucía", resultado.get(0).getNombre());
    }
}
