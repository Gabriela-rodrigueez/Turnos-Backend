package ies.belgrano.turnos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ies.belgrano.turnos.dto.EspecialidadDTO;
import ies.belgrano.turnos.dto.EspecialidadRequestDTO;
import ies.belgrano.turnos.dto.EspecialidadResponseDTO;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;

@ExtendWith(MockitoExtension.class)
class EspecialidadServiceImplTest {

    @Mock
    private EspecialidadRepository especialidadRepository;

    @Mock
    private ProfesionalRepository profesionalRepository;

    @InjectMocks
    private EspecialidadServiceImpl especialidadService;

    private Especialidad cardiologia;

    @BeforeEach
    void setUp() {
        cardiologia = new Especialidad(1L, "Cardiología", "Diagnóstico y tratamiento de patologías cardíacas.");
    }

    @Test
    @DisplayName("crearEspecialidad: Crea y retorna EspecialidadResponseDTO con éxito cuando no existe duplicado")
    void testCrearEspecialidad_Exitoso() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Neurología", "Enfermedades del sistema nervioso.");

        when(especialidadRepository.existsByNombreIgnoreCase("Neurología")).thenReturn(false);
        when(especialidadRepository.save(any(Especialidad.class))).thenAnswer(invocation -> {
            Especialidad e = invocation.getArgument(0);
            e.setId(10L);
            return e;
        });

        EspecialidadResponseDTO resultado = especialidadService.crearEspecialidad(request);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Neurología", resultado.getNombre());
        assertEquals("Enfermedades del sistema nervioso.", resultado.getDescripcion());
        verify(especialidadRepository).save(any(Especialidad.class));
    }

    @Test
    @DisplayName("crearEspecialidad: Lanza ConflictoException cuando el nombre ya existe (insensible a mayúsculas)")
    void testCrearEspecialidad_NombreDuplicado_LanzaConflictoException() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("CARDIOLOGÍA", "Otra descripción.");

        when(especialidadRepository.existsByNombreIgnoreCase("CARDIOLOGÍA")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () ->
                especialidadService.crearEspecialidad(request)
        );

        assertEquals("Ya existe una especialidad médica registrada con el nombre: CARDIOLOGÍA", ex.getMessage());
        verify(especialidadRepository, never()).save(any(Especialidad.class));
    }

    @Test
    @DisplayName("crearEspecialidad: Lanza IllegalArgumentException si el request o nombre es nulo o vacío")
    void testCrearEspecialidad_NombreInvalido_LanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> especialidadService.crearEspecialidad(null));
        assertThrows(IllegalArgumentException.class, () -> especialidadService.crearEspecialidad(new EspecialidadRequestDTO(null)));
        assertThrows(IllegalArgumentException.class, () -> especialidadService.crearEspecialidad(new EspecialidadRequestDTO("   ")));
        verify(especialidadRepository, never()).save(any(Especialidad.class));
    }

    @Test
    @DisplayName("actualizarEspecialidad: Modifica nombre y descripción con éxito cuando no hay colisión")
    void testActualizarEspecialidad_Exitoso() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Cardiología Avanzada", "Nueva descripción clínica.");

        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(cardiologia));
        when(especialidadRepository.existsByNombreIgnoreCaseAndIdNot("Cardiología Avanzada", 1L)).thenReturn(false);
        when(especialidadRepository.save(any(Especialidad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EspecialidadResponseDTO resultado = especialidadService.actualizarEspecialidad(1L, request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Cardiología Avanzada", resultado.getNombre());
        assertEquals("Nueva descripción clínica.", resultado.getDescripcion());
        verify(especialidadRepository).save(cardiologia);
    }

    @Test
    @DisplayName("actualizarEspecialidad: Permite conservar el mismo nombre de la propia especialidad sin colisión")
    void testActualizarEspecialidad_MismoNombre_Exitoso() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Cardiología", "Descripción actualizada.");

        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(cardiologia));
        when(especialidadRepository.existsByNombreIgnoreCaseAndIdNot("Cardiología", 1L)).thenReturn(false);
        when(especialidadRepository.save(any(Especialidad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EspecialidadResponseDTO resultado = especialidadService.actualizarEspecialidad(1L, request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Cardiología", resultado.getNombre());
        assertEquals("Descripción actualizada.", resultado.getDescripcion());
        verify(especialidadRepository).save(cardiologia);
    }

    @Test
    @DisplayName("actualizarEspecialidad: Lanza ConflictoException si el nuevo nombre colisiona con otra especialidad")
    void testActualizarEspecialidad_ColisionNombreConOtra_LanzaConflictoException() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Pediatría", "Descripción.");

        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(cardiologia));
        when(especialidadRepository.existsByNombreIgnoreCaseAndIdNot("Pediatría", 1L)).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () ->
                especialidadService.actualizarEspecialidad(1L, request)
        );

        assertEquals("Ya existe otra especialidad médica registrada con el nombre: Pediatría", ex.getMessage());
        verify(especialidadRepository, never()).save(any(Especialidad.class));
    }

    @Test
    @DisplayName("actualizarEspecialidad: Lanza RecursoNoEncontradoException si el ID no existe")
    void testActualizarEspecialidad_NoExiste_LanzaRecursoNoEncontradoException() {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Oftalmología", "Visión");

        when(especialidadRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () ->
                especialidadService.actualizarEspecialidad(99L, request)
        );

        assertEquals("No se encontró la especialidad médica con ID: 99", ex.getMessage());
        verify(especialidadRepository, never()).save(any(Especialidad.class));
    }

    @Test
    @DisplayName("actualizarEspecialidad: Lanza IllegalArgumentException si el ID o nombre es inválido")
    void testActualizarEspecialidad_ParametrosInvalidos_LanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> especialidadService.actualizarEspecialidad(null, new EspecialidadRequestDTO("Nombre")));
        assertThrows(IllegalArgumentException.class, () -> especialidadService.actualizarEspecialidad(1L, null));
        assertThrows(IllegalArgumentException.class, () -> especialidadService.actualizarEspecialidad(1L, new EspecialidadRequestDTO("  ")));
    }

    @Test
    @DisplayName("obtenerEspecialidadPorId: Retorna EspecialidadResponseDTO cuando la especialidad existe")
    void testObtenerEspecialidadPorId_Exitoso() {
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(cardiologia));

        EspecialidadResponseDTO resultado = especialidadService.obtenerEspecialidadPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Cardiología", resultado.getNombre());
        assertEquals("Diagnóstico y tratamiento de patologías cardíacas.", resultado.getDescripcion());
    }

    @Test
    @DisplayName("obtenerEspecialidadPorId: Lanza RecursoNoEncontradoException cuando el ID no existe")
    void testObtenerEspecialidadPorId_NoExiste_LanzaRecursoNoEncontradoException() {
        when(especialidadRepository.findById(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () ->
                especialidadService.obtenerEspecialidadPorId(999L)
        );

        assertEquals("No se encontró la especialidad médica con ID: 999", ex.getMessage());
    }

    @Test
    @DisplayName("obtenerEspecialidadPorId: Lanza IllegalArgumentException si el ID es nulo")
    void testObtenerEspecialidadPorId_IdNulo_LanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> especialidadService.obtenerEspecialidadPorId(null));
    }

    @Test
    @DisplayName("listarEspecialidades: Retorna lista ordenada de todas las especialidades")
    void testListarEspecialidades_Todas() {
        Especialidad e1 = new Especialidad(1L, "Cardiología", "Desc 1");
        Especialidad e2 = new Especialidad(2L, "Dermatología", "Desc 2");

        when(especialidadRepository.findAll()).thenReturn(List.of(e2, e1));

        List<EspecialidadDTO> resultado = especialidadService.listarEspecialidades();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
    }

    @Test
    @DisplayName("listarEspecialidades: Retorna lista filtrada por profesional")
    void testListarEspecialidades_PorProfesional() {
        Profesional prof = new Profesional();
        prof.setId(5L);
        prof.setEspecialidades(Set.of(cardiologia));

        when(profesionalRepository.findById(5L)).thenReturn(Optional.of(prof));

        List<EspecialidadDTO> resultado = especialidadService.listarEspecialidades(5L);

        assertEquals(1, resultado.size());
        assertEquals("Cardiología", resultado.get(0).getNombre());
    }
}

