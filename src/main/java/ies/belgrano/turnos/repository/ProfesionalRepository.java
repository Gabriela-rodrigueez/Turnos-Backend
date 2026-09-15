package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    Optional<Profesional> findByDni(String dni);
    Optional<Profesional> findByMatricula(String matricula);
    List<Profesional> findByEspecialidadesId(Long especialidadId);
}
