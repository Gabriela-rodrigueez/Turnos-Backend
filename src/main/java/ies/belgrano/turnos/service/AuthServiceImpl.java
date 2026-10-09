package ies.belgrano.turnos.service;

import ies.belgrano.turnos.dto.LoginRequestDTO;
import ies.belgrano.turnos.dto.LoginResponseDTO;
import ies.belgrano.turnos.dto.RegistroRequestDTO;
import ies.belgrano.turnos.exception.ConflictoException;
import ies.belgrano.turnos.exception.NoAutorizadoException;
import ies.belgrano.turnos.exception.RecursoNoEncontradoException;
import ies.belgrano.turnos.model.ObraSocial;
import ies.belgrano.turnos.model.Paciente;
import ies.belgrano.turnos.model.Rol;
import ies.belgrano.turnos.model.Usuario;
import ies.belgrano.turnos.repository.ObraSocialRepository;
import ies.belgrano.turnos.repository.PacienteRepository;
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
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PacienteRepository pacienteRepository,
                           ObraSocialRepository obraSocialRepository,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public LoginResponseDTO registro(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictoException("El correo electrónico " + dto.getEmail() + " ya se encuentra registrado.");
        }

        if (pacienteRepository.findByCuil(dto.getCuil()).isPresent()) {
            throw new ConflictoException("El CUIL " + dto.getCuil() + " ya se encuentra registrado en el sistema.");
        }

        Long osId = (dto.getObraSocialId() != null) ? dto.getObraSocialId() : 4L; // Default: Particular
        ObraSocial obraSocial = obraSocialRepository.findById(osId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la Obra Social especificada."));

        Paciente paciente = new Paciente(
                null,
                dto.getNombre(),
                dto.getApellido(),
                dto.getCuil(),
                dto.getEmail(),
                dto.getTelefono(),
                dto.getFechaNacimiento(),
                obraSocial
        );
        paciente.setObraSocial(obraSocial);
        paciente = pacienteRepository.save(paciente);

        String passwordHash = passwordEncoder.encode(dto.getPassword());
        Usuario usuario = new Usuario(dto.getEmail(), passwordHash, Rol.PACIENTE);
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
                usuario.getEmail(),
                usuario.getRol()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
    	Usuario usuario = usuarioRepository.findByEmail(dto.getIdentificador())
                .orElseThrow(() -> new NoAutorizadoException("Identificador o contraseña incorrectos."));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new NoAutorizadoException("Correo electrónico o contraseña incorrectos.");
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
                usuario.getEmail(),
                usuario.getRol()
        );
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
        }

        return new LoginResponseDTO(
                token,
                usuario.getId(),
                pacienteId,
                profesionalId,
                nombreCompleto,
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}
