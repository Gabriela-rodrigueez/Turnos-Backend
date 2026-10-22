package ies.belgrano.turnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ies.belgrano.turnos.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByLegajo(String legajo);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN u.paciente p WHERE p.dni = :dni")
    Optional<Usuario> findByPacienteDni(@Param("dni") String dni);

    @Query("SELECT u FROM Usuario u JOIN u.profesional prof WHERE LOWER(prof.matricula) = LOWER(:matricula)")
    Optional<Usuario> findByProfesionalMatricula(@Param("matricula") String matricula);

    @Query("SELECT u FROM Usuario u WHERE u.legajo IS NOT NULL AND LOWER(u.legajo) = LOWER(:legajo)")
    Optional<Usuario> findByLegajoIgnoreCase(@Param("legajo") String legajo);
}

