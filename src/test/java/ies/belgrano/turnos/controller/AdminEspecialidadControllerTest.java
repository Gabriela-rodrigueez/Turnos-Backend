package ies.belgrano.turnos.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ies.belgrano.turnos.TurnosApplication;
import ies.belgrano.turnos.dto.EspecialidadRequestDTO;
import ies.belgrano.turnos.model.Especialidad;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.EspecialidadRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
import ies.belgrano.turnos.security.JwtService;

@SpringBootTest(classes = TurnosApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class AdminEspecialidadControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private Especialidad especialidadExistente1;
    private Especialidad especialidadExistente2;
    private String tokenAdmin;
    private String tokenMedico;
    private String tokenPaciente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Especialidades semilla para los tests
        especialidadExistente1 = especialidadRepository.save(new Especialidad(
                "Cardiología Test", "Atención cardiovascular integral."
        ));
        especialidadExistente2 = especialidadRepository.save(new Especialidad(
                "Pediatría Test", "Atención integral del niño y adolescente."
        ));

        // Usuario ADMINISTRADOR
        Usuario admin = new Usuario("admin.esp@saludmza.gob.ar", "hashPass", Rol.ADMINISTRADOR);
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generarToken(admin);

        // Usuario MEDICO
        Usuario medico = new Usuario("medico.esp@saludmza.gob.ar", "hashPass", Rol.MEDICO);
        usuarioRepository.save(medico);
        tokenMedico = jwtService.generarToken(medico);

        // Usuario PACIENTE
        Usuario paciente = new Usuario("paciente.esp@gmail.com", "hashPass", Rol.PACIENTE);
        usuarioRepository.save(paciente);
        tokenPaciente = jwtService.generarToken(paciente);
    }

    // =========================================================================
    // 1. POST /api/v1/admin/especialidades (ALTA)
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/admin/especialidades - Camino feliz: Admin crea especialidad retorna 201 Created con encabezado Location y DTO")
    void testCrearEspecialidad_Exitoso_Retorna201CreatedConLocation() throws Exception {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO(
                "Neurología",
                "Diagnóstico y tratamiento de patologías neurológicas."
        );

        mockMvc.perform(post("/api/v1/admin/especialidades")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/admin/especialidades/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Neurología")))
                .andExpect(jsonPath("$.descripcion", is("Diagnóstico y tratamiento de patologías neurológicas.")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/especialidades - Validación obligatoria: Nombre en blanco retorna 400 Bad Request")
    void testCrearEspecialidad_NombreVacio_Retorna400BadRequest() throws Exception {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("   ", "Descripción válida");

        mockMvc.perform(post("/api/v1/admin/especialidades")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Datos de Solicitud Inválidos")))
                .andExpect(jsonPath("$.detalles.nombre", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/admin/especialidades - Nombre duplicado insensible a mayúsculas/minúsculas retorna 409 Conflict")
    void testCrearEspecialidad_NombreDuplicado_Retorna409Conflict() throws Exception {
        // Intenta dar de alta una especialidad con nombre en minúsculas coincidente con especialidadExistente1 ("Cardiología Test")
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("cardiología test", "Otra descripción.");

        mockMvc.perform(post("/api/v1/admin/especialidades")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflicto de Datos")))
                .andExpect(jsonPath("$.message", containsString("cardiología test")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/especialidades - Rechazo por rol no permitido: MEDICO retorna 403 Forbidden")
    void testCrearEspecialidad_RolMedico_Retorna403Forbidden() throws Exception {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Gastroenterología", "Descripción");

        mockMvc.perform(post("/api/v1/admin/especialidades")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Acceso Denegado")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/especialidades - Rechazo por rol no permitido: PACIENTE retorna 403 Forbidden")
    void testCrearEspecialidad_RolPaciente_Retorna403Forbidden() throws Exception {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Gastroenterología", "Descripción");

        mockMvc.perform(post("/api/v1/admin/especialidades")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Acceso Denegado")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/especialidades - Sin autenticación retorna 401 Unauthorized")
    void testCrearEspecialidad_SinToken_Retorna401Unauthorized() throws Exception {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Gastroenterología", "Descripción");

        mockMvc.perform(post("/api/v1/admin/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    // =========================================================================
    // 2. PUT /api/v1/admin/especialidades/{id} (EDICIÓN)
    // =========================================================================

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - Camino feliz: Modifica nombre y descripción retorna 200 OK")
    void testActualizarEspecialidad_Exitoso_Retorna200Ok() throws Exception {
        Long id = especialidadExistente1.getId();
        EspecialidadRequestDTO request = new EspecialidadRequestDTO(
                "Cardiología y Hemodinamia",
                "Diagnóstico, tratamiento y cateterismos."
        );

        mockMvc.perform(put("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.nombre", is("Cardiología y Hemodinamia")))
                .andExpect(jsonPath("$.descripcion", is("Diagnóstico, tratamiento y cateterismos.")));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - Conserva el mismo nombre actualizando descripción retorna 200 OK")
    void testActualizarEspecialidad_MismoNombre_Retorna200Ok() throws Exception {
        Long id = especialidadExistente1.getId();
        EspecialidadRequestDTO request = new EspecialidadRequestDTO(
                "Cardiología Test",
                "Nueva descripción médica actualizada."
        );

        mockMvc.perform(put("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.nombre", is("Cardiología Test")))
                .andExpect(jsonPath("$.descripcion", is("Nueva descripción médica actualizada.")));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - Validación obligatoria: Nombre vacío retorna 400 Bad Request")
    void testActualizarEspecialidad_NombreVacio_Retorna400BadRequest() throws Exception {
        Long id = especialidadExistente1.getId();
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("", "Descripción");

        mockMvc.perform(put("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detalles.nombre", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - ID inexistente retorna 404 Not Found")
    void testActualizarEspecialidad_NoExiste_Retorna404NotFound() throws Exception {
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Oftalmología", "Visión");

        mockMvc.perform(put("/api/v1/admin/especialidades/999999")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso No Encontrado")));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - Colisión de nombre con otra especialidad existente retorna 409 Conflict")
    void testActualizarEspecialidad_ColisionNombre_Retorna409Conflict() throws Exception {
        // Intenta renombrar especialidadExistente2 ("Pediatría Test") al nombre de especialidadExistente1 ("cardiología test")
        Long id = especialidadExistente2.getId();
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("cardiología test", "Nueva desc");

        mockMvc.perform(put("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflicto de Datos")));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - Rechazo por rol no permitido: PACIENTE retorna 403 Forbidden")
    void testActualizarEspecialidad_RolPaciente_Retorna403Forbidden() throws Exception {
        Long id = especialidadExistente1.getId();
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Cardiología", "Desc");

        mockMvc.perform(put("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/especialidades/{id} - Rechazo por rol no permitido: MEDICO retorna 403 Forbidden")
    void testActualizarEspecialidad_RolMedico_Retorna403Forbidden() throws Exception {
        Long id = especialidadExistente1.getId();
        EspecialidadRequestDTO request = new EspecialidadRequestDTO("Cardiología", "Desc");

        mockMvc.perform(put("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    // =========================================================================
    // 3. GET /api/v1/admin/especialidades/{id} (DETALLE)
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/admin/especialidades/{id} - Camino feliz: Admin consulta especialidad por ID retorna 200 OK")
    void testObtenerEspecialidadPorId_Exitoso_Retorna200Ok() throws Exception {
        Long id = especialidadExistente1.getId();

        mockMvc.perform(get("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.nombre", is("Cardiología Test")))
                .andExpect(jsonPath("$.descripcion", is("Atención cardiovascular integral.")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/especialidades/{id} - ID inexistente retorna 404 Not Found")
    void testObtenerEspecialidadPorId_NoExiste_Retorna404NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/especialidades/999999")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Recurso No Encontrado")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/especialidades/{id} - Rechazo por rol no permitido: PACIENTE retorna 403 Forbidden")
    void testObtenerEspecialidadPorId_RolPaciente_Retorna403Forbidden() throws Exception {
        Long id = especialidadExistente1.getId();

        mockMvc.perform(get("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/especialidades/{id} - Rechazo por rol no permitido: MEDICO retorna 403 Forbidden")
    void testObtenerEspecialidadPorId_RolMedico_Retorna403Forbidden() throws Exception {
        Long id = especialidadExistente1.getId();

        mockMvc.perform(get("/api/v1/admin/especialidades/" + id)
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/especialidades - Admin lista todas las especialidades retorna 200 OK")
    void testListarEspecialidadesAdmin_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/admin/especialidades")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

