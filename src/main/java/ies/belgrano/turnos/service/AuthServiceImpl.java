package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.LoginRequestDTO;
import ies.belgrano.turnos.dto.LoginResponseDTO;
import ies.belgrano.turnos.dto.RegistroRequestDTO;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.NoAutorizadoException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.Administrador; 
import ies.belgrano.turnos.model.ObraSocial;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.AdministradorRepository; 
import ies.belgrano.turnos.repository.ObraSocialRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
import ies.belgrano.turnos.repository.ProfesionalRepository;
import ies.belgrano.turnos.repository.UsuarioRepository;
import ies.belgrano.turnos.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AdministradorRepository administradorRepository; 
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PacienteRepository pacienteRepository,
                           ObraSocialRepository obraSocialRepository,
                           ProfesionalRepository profesionalRepository, 
                           AdministradorRepository administradorRepository, 
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.profesionalRepository = profesionalRepository;
        this.administradorRepository = administradorRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public LoginResponseDTO registro(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictoException("El correo electrónico " + dto.getEmail() + " ya se encuentra registrado.");
        }

        // Corregido el paréntesis de dto.getDni()
        if (pacienteRepository.findByDni(dto.getDni()).isPresent()) {
            throw new ConflictoException("El DNI " + dto.getDni() + " ya se encuentra registrado en el sistema.");
        }

        Long osId = (dto.getObraSocialId() != null) ? dto.getObraSocialId() : 4L; // Default: Particular
        ObraSocial obraSocial = obraSocialRepository.findById(osId)
                // Eliminada la palabra fantasma "mensaje:"
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social especificada."));

        // Eliminado el "id: null, ..." que marcaba error
        Paciente paciente = new Paciente(
                null,
                dto.getNombre(),
                dto.getApellido(),
                dto.getDni(),
                dto.getEmail(),
                dto.getTelefono(),
                dto.getFechaNacimiento(),
                obraSocial
        );
        paciente.setObraSocial(obraSocial);
        paciente.setGenero(dto.getGender());
        paciente.setTieneObraSocial("si".equalsIgnoreCase(dto.getHasInsurance()) || "true".equalsIgnoreCase(dto.getHasInsurance()));
        paciente.setProveedorObraSocial(dto.getProvider());
        paciente.setNumeroAfiliado(dto.getAffiliateNum());
        paciente.setPlanObraSocial(dto.getPlan());

        paciente = pacienteRepository.save(paciente);

        String passwordHash = passwordEncoder.encode(dto.getPassword());
        Usuario usuario = new Usuario(dto.getEmail(), passwordHash, Rol.PACIENTE);
        usuario.setDni(dto.getDni());
        usuario.setPaciente(paciente);
        usuario = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario);
        String nombreCompleto = paciente.getNombre() + " " + paciente.getApellido();

        return new LoginResponseDTO(
                token,
                usuario.getId(),
                paciente.getId(),
                null,
                nombreCompleto,
                usuario.getDni(),
                usuario.getRol()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        
        // 1. PRIMERO VERIFICAMOS SI ESTÁ INTENTANDO ENTRAR UN MÉDICO (CON MATRÍCULA)
        if (dto.getMatricula() != null && !dto.getMatricula().isEmpty()) {
            
            Profesional profesional = profesionalRepository.findByMatricula(dto.getMatricula())
                .orElseThrow(() -> new NoAutorizadoException("Matrícula no encontrada."));
            
            Usuario usuario = usuarioRepository.findByProfesional(profesional)
                .orElseThrow(() -> new NoAutorizadoException("Usuario médico no encontrado."));
                
            if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
                throw new NoAutorizadoException("Contraseña incorrecta.");
            }
            
            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                throw new NoAutorizadoException("La cuenta de usuario se encuentra deshabilitada.");
            }
            
            String token = jwtService.generarToken(usuario);
            String nombreCompleto = "Dr/a. " + profesional.getNombre() + " " + profesional.getApellido();
            
            return new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    null, // null porque no es paciente
                    profesional.getId(),
                    nombreCompleto,
                    usuario.getDni(),
                    usuario.getRol()
            );
        }
        
        // 2. SEGUNDO VERIFICAMOS SI ESTÁ INTENTANDO ENTRAR UN ADMINISTRADOR (CON LEGAJO)
        else if (dto.getLegajo() != null && !dto.getLegajo().isEmpty()) {
            System.out.println("\n--- INICIANDO INTENTO DE LOGIN ADMIN ---");
            System.out.println("1. Legajo recibido desde Angular: [" + dto.getLegajo() + "]");
            
            Administrador admin = administradorRepository.findByLegajo(dto.getLegajo())
                .orElseThrow(() -> {
                    System.out.println("-> FALLA: No se encontró el legajo en la tabla 'administradores'.");
                    return new NoAutorizadoException("Legajo no encontrado.");
                });
            System.out.println("2. ¡Legajo encontrado en BD! Buscando usuario enlazado...");
                
            Usuario usuario = usuarioRepository.findByAdministrador(admin)
                .orElseThrow(() -> {
                    System.out.println("-> FALLA: El administrador existe, pero su ID no está enlazado en la tabla 'usuarios'.");
                    return new NoAutorizadoException("Usuario administrador no encontrado.");
                });
            System.out.println("3. ¡Usuario encontrado! Verificando la contraseña...");
                
            if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
                System.out.println("-> FALLA: El hash de la contraseña no coincide.");
                throw new NoAutorizadoException("Contraseña incorrecta.");
            }
            
            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                throw new NoAutorizadoException("La cuenta de usuario se encuentra deshabilitada.");
            }
            
            System.out.println("4. ¡ÉXITO! Todo coincide perfecto. Permitiendo acceso...");
            String token = jwtService.generarToken(usuario);
            String nombreCompleto = admin.getNombre() + " " + admin.getApellido();
            
            return new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    null,
                    null,
                    nombreCompleto,
                    usuario.getDni(),
                    usuario.getRol()
            );
        }
        
        // 3. SI NO ENVIÓ MATRÍCULA NI LEGAJO, ENTONCES ESTÁ INTENTANDO ENTRAR UN PACIENTE (CON DNI)
        else if (dto.getDni() != null && !dto.getDni().isEmpty()) {
            
            Usuario usuario = usuarioRepository.findByDni(dto.getDni())
                    .orElseThrow(() -> new NoAutorizadoException("DNI o contraseña incorrectos."));

            if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
                throw new NoAutorizadoException("DNI o contraseña incorrectos.");
            }

            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                throw new NoAutorizadoException("La cuenta de usuario se encuentra deshabilitada.");
            }

            String token = jwtService.generarToken(usuario);
            
            Long pacienteId = usuario.getPaciente() != null ? usuario.getPaciente().getId() : null;
            Long profesionalId = usuario.getProfesional() != null ? usuario.getProfesional().getId() : null;
            
            String nombreCompleto = "Usuario Sistema";
            if (usuario.getPaciente() != null) {
                nombreCompleto = usuario.getPaciente().getNombre() + " " + usuario.getPaciente().getApellido();
            } else if (usuario.getProfesional() != null) {
                nombreCompleto = "Dr/a. " + usuario.getProfesional().getNombre() + " " + usuario.getProfesional().getApellido();
            }

            return new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    pacienteId,
                    profesionalId,
                    nombreCompleto,
                    usuario.getDni(),
                    usuario.getRol()
            );
        }
        
        // 4. SI NO MANDÓ NADA
        throw new NoAutorizadoException("Debe ingresar credenciales válidas.");
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO obtenerUsuarioActual(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario activo."));

        String token = jwtService.generarToken(usuario);
        Long pacienteId = usuario.getPaciente() != null ? usuario.getPaciente().getId() : null;
        Long profesionalId = usuario.getProfesional() != null ? usuario.getProfesional().getId() : null;
        String nombreCompleto = "Usuario Sistema";

        if (usuario.getPaciente() != null) {
            nombreCompleto = usuario.getPaciente().getNombre() + " " + usuario.getPaciente().getApellido();
        } else if (usuario.getProfesional() != null) {
            nombreCompleto = "Dr/a. " + usuario.getProfesional().getNombre() + " " + usuario.getProfesional().getApellido();
        } else if (usuario.getAdministrador() != null) {
            nombreCompleto = usuario.getAdministrador().getNombre() + " " + usuario.getAdministrador().getApellido();
        }

        return new LoginResponseDTO(
                token,
                usuario.getId(),
                pacienteId,
                profesionalId,
                nombreCompleto,
                usuario.getDni(),
                usuario.getRol()
        );
    }
}