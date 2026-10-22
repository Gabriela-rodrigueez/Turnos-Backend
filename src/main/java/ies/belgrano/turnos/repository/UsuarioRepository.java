package ies.belgrano.turnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ies.belgrano.turnos.model.Administrador;
import ies.belgrano.turnos.model.Profesional;
import ies.belgrano.turnos.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByDni(String dni);
    Optional<Usuario> findByProfesional(Profesional profesional);
    Optional<Usuario> findByAdministrador(Administrador administrador);

    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
}
