package com.example.acceso.repository;

import com.example.acceso.model.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Cuota}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para las cuotas, así como consultas personalizadas derivadas
 * de los nombres de los métodos.</p>
 */
@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    /**
     * Obtiene una lista de todas las cuotas cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir las cuotas anuladas (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link Cuota}.
     */
    List<Cuota> findAllByEstadoNot(Integer estado);

    /**
     * Busca todas las cuotas asociadas a una venta específica.
     *
     * @param ventaId El ID de la venta para la cual se buscan las cuotas.
     * @return Una lista de objetos {@link Cuota} asociados a la venta.
     */
    List<Cuota> findByVentaId(Long ventaId);

    /**
     * Cuenta el número de cuotas que tienen un estado específico.
     * <p>Por ejemplo, se puede usar para contar cuotas pendientes (estado = 0) o pagadas (estado = 1).</p>
     *
     * @param estado El estado de las cuotas a contar.
     * @return El número de cuotas que tienen el estado especificado.
     */
    Long countByEstado(Integer estado);

}
