package com.example.acceso.repository;

import com.example.acceso.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT v FROM Venta v JOIN FETCH v.detalleVentas d WHERE d.producto.id = :productoId ORDER BY v.fecha DESC")
    List<Venta> findVentasByProductoId(@Param("productoId") Long productoId);

    List<Venta> findAllByEstadoNot(Integer estado);

    Long countByEstado(Integer estado);

}
