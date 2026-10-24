package ies.belgrano.turnos.repository.specification;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import ies.belgrano.turnos.model.Paciente;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * Especificaciones JPA Criteria para la entidad Paciente.
 * Permite construir predicados dinámicos con insensibilidad a mayúsculas y tildes en base de datos.
 */
public final class PacienteSpecification {

    private PacienteSpecification() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Construye una especificación para buscar pacientes por nombre, apellido, DNI
     * o nombre completo, insensible a mayúsculas y acentos diacríticos.
     *
     * @param filtro Criterio de búsqueda ingresado por el usuario.
     * @return Specification de Paciente con los predicados correspondientes.
     */
    public static Specification<Paciente> conFiltro(String filtro) {
        return (root, query, cb) -> {
            if (filtro == null || filtro.trim().isEmpty()) {
                return cb.conjunction();
            }

            Set<String> variantes = generarVariantes(filtro);
            List<Predicate> predicadosTotales = new ArrayList<>();

            for (String variante : variantes) {
                String pattern = "%" + variante + "%";

                Predicate nombrePredicate = cb.like(cb.lower(root.get("nombre")), pattern);
                Predicate apellidoPredicate = cb.like(cb.lower(root.get("apellido")), pattern);
                Predicate dniPredicate = cb.like(cb.lower(root.get("dni")), pattern);

                // Concatenación "nombre apellido"
                Expression<String> nombreCompleto1 = cb.concat(
                        cb.concat(cb.lower(root.get("nombre")), " "),
                        cb.lower(root.get("apellido"))
                );
                Predicate nc1Predicate = cb.like(nombreCompleto1, pattern);

                // Concatenación "apellido nombre"
                Expression<String> nombreCompleto2 = cb.concat(
                        cb.concat(cb.lower(root.get("apellido")), " "),
                        cb.lower(root.get("nombre"))
                );
                Predicate nc2Predicate = cb.like(nombreCompleto2, pattern);

                predicadosTotales.add(nombrePredicate);
                predicadosTotales.add(apellidoPredicate);
                predicadosTotales.add(dniPredicate);
                predicadosTotales.add(nc1Predicate);
                predicadosTotales.add(nc2Predicate);
            }

            return cb.or(predicadosTotales.toArray(new Predicate[0]));
        };
    }

    /**
     * Genera variantes ortográficas con y sin acento para permitir búsquedas insensibles a tildes
     * en bases de datos relacionales sin requerir extensiones propietarias.
     */
    private static Set<String> generarVariantes(String texto) {
        Set<String> variantes = new LinkedHashSet<>();
        if (texto == null || texto.isBlank()) {
            return variantes;
        }

        String limpio = texto.trim().toLowerCase();
        variantes.add(limpio);

        // Variante normalizada sin tildes
        String sinAcentos = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        variantes.add(sinAcentos);

        // Generar variantes con tildes comunes para vocales
        char[] vocales = {'a', 'e', 'i', 'o', 'u'};
        char[] conTildes = {'á', 'é', 'í', 'ó', 'ú'};

        for (int i = 0; i < vocales.length; i++) {
            if (sinAcentos.indexOf(vocales[i]) != -1) {
                String conAcento = sinAcentos.replace(vocales[i], conTildes[i]);
                variantes.add(conAcento);
            }
        }

        return variantes;
    }
}
