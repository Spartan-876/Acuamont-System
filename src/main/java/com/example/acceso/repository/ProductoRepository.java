package com.example.acceso.repository;

import com.example.acceso.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Producto}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para los productos, así como consultas personalizadas derivadas
 * de los nombres de los métodos.</p>
 */
@Repository
public interface ProductoRepository  extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su nombre. La búsqueda es sensible a mayúsculas y minúsculas
     * a menos que se configure lo contrario en la base de datos.
     *
     * @param nombre El nombre del producto a buscar.
     * @return Un {@link Optional} que contiene el producto si se encuentra, o un Optional vacío si no.
     */
    Optional<Producto> findByNombre(String nombre);

    /**
     * Verifica si existe un producto con un nombre específico.
     * <p>Este método es más eficiente que {@code findByNombre().isPresent()} si solo se necesita
     * saber si el producto existe, ya que puede traducirse en una consulta SQL más optimizada (COUNT o EXISTS).</p>
     *
     * @param nombre El nombre del producto a verificar.
     * @return {@code true} si el producto existe, {@code false} en caso contrario.
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene una lista de todos los productos cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir los productos eliminados lógicamente (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link Producto}.
     */
    List<Producto> findAllByEstadoNot(Integer estado);

    /**
     * Cuenta el número de productos cuyo estado no coincide con el valor proporcionado.
     *
     * @param estado El estado a excluir en el conteo.
     * @return El número de productos que no tienen el estado especificado.
     */
    Long countByEstadoNot(Integer estado);

}
