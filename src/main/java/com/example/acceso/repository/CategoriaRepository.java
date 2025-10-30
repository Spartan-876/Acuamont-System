package com.example.acceso.repository;

import com.example.acceso.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Categoria}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para las categorías, así como consultas personalizadas derivadas
 * de los nombres de los métodos.</p>
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su nombre. La búsqueda es sensible a mayúsculas y minúsculas
     * a menos que se configure lo contrario en la base de datos.
     *
     * @param nombre El nombre de la categoría a buscar.
     * @return Un {@link Optional} que contiene la categoría si se encuentra, o un Optional vacío si no.
     */
    Optional<Categoria> findByNombre(String nombre);

    /**
     * Verifica si existe una categoría con un nombre específico.
     * <p>Este método es más eficiente que {@code findByNombre().isPresent()} si solo se necesita
     * saber si la categoría existe, ya que puede traducirse en una consulta SQL más optimizada (COUNT o EXISTS).</p>
     *
     * @param nombre El nombre de la categoría a verificar.
     * @return {@code true} si la categoría existe, {@code false} en caso contrario.
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene una lista de todas las categorías cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir las categorías eliminadas lógicamente (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link Categoria}.
     */
    List<Categoria> findAllByEstadoNot(Integer estado);

    /**
     * Cuenta el número de categorías cuyo estado no coincide con el valor proporcionado.
     *
     * @param estado El estado a excluir en el conteo.
     * @return El número de categorías que no tienen el estado especificado.
     */
    Long countByEstadoNot(Integer estado);

    /**
     * Obtiene una lista de todas las categorías cuyo estado se interpreta como verdadero.
     * <p><strong>Nota:</strong> El comportamiento de este método depende de cómo la base de datos
     * y JPA interpretan un valor booleano contra un campo de tipo {@link Integer}.
     * Para una mayor claridad, se recomienda usar {@code findAllByEstado(1)}.</p>
     *
     * @return Una lista de categorías activas.
     */
    List<Categoria> findByEstadoTrue();

}
