package com.example.acceso.repository;

import com.example.acceso.model.SerieComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link SerieComprobante}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para las series de comprobantes, así como consultas personalizadas derivadas
 * de los nombres de los métodos.</p>
 */
@Repository
public interface SerieComprobanteRepository extends JpaRepository<SerieComprobante, Long> {

    /**
     * Busca una serie de comprobante por su nombre (ej. "F001").
     *
     * @param nombre El nombre de la serie a buscar.
     * @return Un {@link Optional} que contiene la serie si se encuentra, o un Optional vacío si no.
     */
    Optional<SerieComprobante> findByNombre(String nombre);

    /**
     * Verifica si existe una serie de comprobante con un nombre específico.
     * <p>Este método es más eficiente que {@code findByNombre().isPresent()} si solo se necesita
     * saber si la serie existe.</p>
     *
     * @param nombre El nombre de la serie a verificar.
     * @return {@code true} si la serie existe, {@code false} en caso contrario.
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene una lista de todas las series de comprobante cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir las series eliminadas lógicamente (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link SerieComprobante}.
     */
    List<SerieComprobante> findAllByEstadoNot(Integer estado);

    /**
     * Obtiene una lista de todas las series de comprobante cuyo estado se interpreta como verdadero.
     * <p><strong>Nota:</strong> El comportamiento de este método depende de cómo la base de datos
     * y JPA interpretan un valor booleano contra un campo de tipo {@link Integer}.
     * Para una mayor claridad, se recomienda usar {@code findAllByEstado(1)}.</p>
     *
     * @return Una lista de series de comprobante activas.
     */
    List<SerieComprobante> findAllByEstadoTrue();

}
