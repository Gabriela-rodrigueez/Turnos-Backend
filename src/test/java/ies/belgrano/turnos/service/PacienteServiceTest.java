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
    @DisplayName("buscarPorCuil: Retorna PacienteResponseDTO cuando el paciente existe")
    void testBuscarPorCuil_Exitoso() {
        String cuil = "27351112229";
        ObraSocial osep = new ObraSocial("OSEP Mendoza", "OSEP-001");
        osep.setId(1L);
        Paciente paciente = new Paciente(1L, "Juan", "González", cuil, "juan@gmail.com", "2614001111", LocalDate.of(1990, 5, 15), osep);

        when(pacienteRepository.findByCuil(cuil)).thenReturn(Optional.of(paciente));

        PacienteResponseDTO resultado = pacienteService.buscarPorCuil(cuil);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("González", resultado.getApellido());
        assertEquals(cuil, resultado.getCuil());
        assertEquals("OSEP Mendoza", resultado.getObraSocialNombre());
        verify(pacienteRepository).findByCuil(cuil);
    }

    @Test
    @DisplayName("buscarPorCuil: Lanza RecursoNoEncontradoException cuando el paciente no existe")
    void testBuscarPorCuil_NoExiste_LanzaExcepcion() {
        String cuil = "27999999990";
        when(pacienteRepository.findByCuil(cuil)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () -> {
            pacienteService.buscarPorCuil(cuil);
        });

        assertEquals("No se encontró ningún paciente con el CUIL: " + cuil, ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorCuil: Lanza IllegalArgumentException si el CUIL es nulo o vacío")
    void testBuscarPorCuil_CuilInvalido_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> pacienteService.buscarPorCuil(null));
        assertThrows(IllegalArgumentException.class, () -> pacienteService.buscarPorCuil("   "));
    }

    @Test
    @DisplayName("listarPacientes: Retorna todos los pacientes cuando el filtro es nulo o vacío")
    void testListarPacientes_SinFiltro() {
        Paciente p1 = new Paciente(1L, "Juan", "González", "35111222", "juan@gmail.com", "2614001111", LocalDate.of(1990, 5, 15), null);
        Paciente p2 = new Paciente(2L, "Lucía", "Martínez", "40222333", "lucia@gmail.com", "2614002222", LocalDate.of(1997, 11, 20), null);

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
        Paciente p1 = new Paciente(1L, "Juan", "González", "35111222", "juan@gmail.com", "2614001111", LocalDate.of(1990, 5, 15), null);
        Paciente p2 = new Paciente(2L, "Lucía", "Martínez", "40222333", "lucia@gmail.com", "2614002222", LocalDate.of(1997, 11, 20), null);

        when(pacienteRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PacienteResponseDTO> resultado = pacienteService.listarPacientes(filtro);

        assertEquals(1, resultado.size());
        assertEquals("González", resultado.get(0).getApellido());
        verify(pacienteRepository).findAll();
    }

    @Test
    @DisplayName("listarPacientes: Búsqueda insensible a tildes y mayúsculas")
    void testListarPacientes_InsensibleTildes() {
        Paciente p1 = new Paciente(1L, "Lucía", "Martínez", "40222333", "lucia@gmail.com", "2614002222", LocalDate.of(1997, 11, 20), null);
        when(pacienteRepository.findAll()).thenReturn(List.of(p1));

        List<PacienteResponseDTO> resultado = pacienteService.listarPacientes("lucia");

        assertEquals(1, resultado.size());
        assertEquals("Lucía", resultado.get(0).getNombre());
    }
}
