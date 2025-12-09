package com.example.acceso.repository;

import com.example.acceso.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Spring Data JPA para la entidad {@link DetalleVenta}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para los detalles de venta. Al extender {@link JpaRepository},
 * se heredan automáticamente métodos como {@code save()}, {@code findById()}, {@code findAll()}, etc.</p>
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

}
