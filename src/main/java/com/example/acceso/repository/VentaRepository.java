package com.example.acceso.repository;

import com.example.acceso.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Venta}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para las ventas, así como consultas personalizadas para
 * obtener datos específicos relacionados con las ventas.</p>
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Busca todas las ventas que incluyen un producto específico.
     * <p>Utiliza {@code JOIN FETCH} para cargar de forma anticipada los detalles de la venta
     * y evitar el problema N+1. Las ventas se ordenan por fecha de forma descendente.</p>
     *
     * @param productoId El ID del producto a buscar en los detalles de venta.
     * @return Una lista de objetos {@link Venta} que contienen el producto especificado.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.detalleVentas d WHERE d.producto.id = :productoId ORDER BY v.fecha DESC")
    List<Venta> findVentasByProductoId(@Param("productoId") Long productoId);

    /**
     * Obtiene una lista de todas las ventas cuyo estado no coincide con el valor proporcionado.
     * <p>Se utiliza comúnmente para excluir las ventas anuladas (estado = 2).</p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link Venta}.
     */
    List<Venta> findAllByEstadoNot(Integer estado);

    /**
     * Cuenta el número de ventas que tienen un estado específico.
     * <p>Por ejemplo, se puede usar para contar ventas pagadas (estado = 1) o pendientes (estado = 0).</p>
     *
     * @param estado El estado de las ventas a contar.
     * @return El número de ventas que tienen el estado especificado.
     */
    Long countByEstado(Integer estado);

    /**
     * Calcula la suma total de las ventas del día actual que no están anuladas.
     * <p>La consulta se ejecuta directamente en la base de datos para mayor eficiencia.</p>
     *
     * @return Un {@link BigDecimal} con la suma total. Devuelve 0 si no hay ventas.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado <> 2 AND FUNCTION('DATE', v.fecha) = CURRENT_DATE")
    BigDecimal sumTotalVentasDelDia();

    /**
     * Calcula la suma total de las ventas del mes actual que no están anuladas.
     * <p>La consulta se ejecuta directamente en la base de datos para mayor eficiencia.</p>
     *
     * @return Un {@link BigDecimal} con la suma total. Devuelve 0 si no hay ventas.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado <> 2 AND YEAR(v.fecha) = YEAR(CURRENT_DATE) AND MONTH(v.fecha) = MONTH(CURRENT_DATE)")
    BigDecimal sumTotalVentasDelMes();

    /**
     * Calcula la suma total de la deuda pendiente de todas las ventas que no están anuladas.
     * <p>La consulta se ejecuta directamente en la base de datos para mayor eficiencia.</p>
     *
     * @return Un {@link BigDecimal} con la suma total de la deuda. Devuelve 0 si no hay deudas.
     */
    @Query("SELECT COALESCE(SUM(v.deuda), 0) FROM Venta v WHERE v.estado <> 2")
    BigDecimal sumTotalDeuda();

    List<Venta> findAllByEstado(Integer estado);

}
