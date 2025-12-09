package com.example.acceso.repository;

import com.example.acceso.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Pago}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para los pagos, así como consultas personalizadas.</p>
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Busca todos los pagos asociados a una venta específica, navegando a través de la relación con Cuota.
     *
     * @param ventaId El ID de la venta para la cual se buscan los pagos.
     * @return Una lista de objetos {@link Pago} asociados a la venta.
     */
    @Query("SELECT p FROM Pago p WHERE p.cuota.venta.id = :ventaId")
    List<Pago> findPagosByVentaId(@Param("ventaId") Long ventaId);

}
