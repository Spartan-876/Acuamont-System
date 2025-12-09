package com.example.acceso.repository;

import com.example.acceso.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link TipoMovimiento}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para los tipos de movimiento de inventario, así como consultas personalizadas.</p>
 */
@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimiento, Long> {

    /**
     * Obtiene una lista de todos los tipos de movimiento cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir los tipos de movimiento eliminados lógicamente (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link TipoMovimiento}.
     */
    List<TipoMovimiento> findAllByEstadoNot(Integer estado);

}
