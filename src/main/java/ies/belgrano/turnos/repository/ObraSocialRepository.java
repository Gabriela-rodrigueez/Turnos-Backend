package ies.belgrano.turnos.repository;

import ies.belgrano.turnos.model.ObraSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObraSocialRepository extends JpaRepository<ObraSocial, Long> {
    Optional<ObraSocial> findByNombreIgnoreCase(String nombre);
    Optional<ObraSocial> findByCodigo(String codigo);
}
