package com.example.acceso.repository;

import com.example.acceso.DTO.ReporteUtilidadProductoDTO;
import com.example.acceso.DTO.ReporteUtilidadVentaDTO;
import com.example.acceso.DTO.ReporteUttilidadUsuarioDTO;
import com.example.acceso.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportesRepository extends JpaRepository<Venta,Long> {
    @Query(value = """
        SELECT 
            CONCAT(sc.serie, '-', LPAD(v.correlativo, 9, '0')) AS documento,
            c.nombre AS cliente,
            v.fecha AS fecha,
            v.total AS totalVenta,
            SUM((dv.precio_unitario - p.precio_compra) * dv.cantidad) AS utilidad
        FROM ventas v
        INNER JOIN clientes c ON v.id_cliente = c.id
        INNER JOIN series_comprobante sc ON v.id_serie_comprobante = sc.id
        INNER JOIN detalle_venta dv ON v.id = dv.id_venta
        INNER JOIN productos p ON dv.id_producto = p.id        
        WHERE v.estado = 1
        GROUP BY v.id, sc.serie, v.correlativo, c.nombre, v.fecha, v.total
        ORDER BY v.fecha DESC
        """, nativeQuery = true)
    List<ReporteUtilidadVentaDTO> obtenerUtilidadPorVenta();


    @Query(value = """
        SELECT 
            CONCAT(sc.serie, '-', v.correlativo) AS documento,
            c.nombre AS cliente,
            v.fecha AS fecha,
            v.total AS totalVenta,
            SUM((dv.precio_unitario - p.precio_compra) * dv.cantidad) AS utilidad
        FROM ventas v
        INNER JOIN clientes c ON v.id_cliente = c.id
        INNER JOIN series_comprobante sc ON v.id_serie_comprobante = sc.id
        INNER JOIN detalle_venta dv ON v.id = dv.id_venta
        INNER JOIN productos p ON dv.id_producto = p.id
        WHERE v.estado = 1 
          AND v.fecha BETWEEN :fechaInicio AND :fechaFin
        GROUP BY v.id, sc.serie, v.correlativo, c.nombre, v.fecha, v.total
        ORDER BY v.fecha DESC
        """, nativeQuery = true)
    List<ReporteUtilidadVentaDTO> obtenerUtilidadPorVentaRango(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query(value = """
        SELECT 
            u.nombre AS usuario,
            COUNT(DISTINCT v.id) AS cantidadVentas,
            SUM((dv.precio_unitario - COALESCE(p.precio_compra, 0)) * dv.cantidad) AS utilidad
        FROM ventas v
        INNER JOIN usuarios u ON v.id_usuario = u.id
        INNER JOIN detalle_venta dv ON v.id = dv.id_venta
        INNER JOIN productos p ON dv.id_producto = p.id
        WHERE v.estado = 1 or v.estado = 0
        GROUP BY u.id, u.nombre
        ORDER BY utilidad DESC
        """, nativeQuery = true)
    List<ReporteUttilidadUsuarioDTO> obtenerUtilidadPorUsuario();

    @Query(value = """
        SELECT 
            u.nombre AS usuario,
            COUNT(DISTINCT v.id) AS cantidadVentas,
            SUM((dv.precio_unitario - COALESCE(p.precio_compra, 0)) * dv.cantidad) AS utilidad
        FROM ventas v
        INNER JOIN usuarios u ON v.id_usuario = u.id
        INNER JOIN detalle_venta dv ON v.id = dv.id_venta
        INNER JOIN productos p ON dv.id_producto = p.id
        WHERE v.estado = 1 
          AND v.fecha BETWEEN :inicio AND :fin
        GROUP BY u.id, u.nombre
        ORDER BY utilidad DESC
        """, nativeQuery = true)
    List<ReporteUttilidadUsuarioDTO> obtenerUtilidadPorUsuarioRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query(value = """
        SELECT 
            p.nombre AS producto,
            SUM(dv.cantidad) AS cantidadVendida,
            SUM(dv.subtotal) AS totalVenta,
            SUM((dv.precio_unitario - COALESCE(p.precio_compra, 0)) * dv.cantidad) AS utilidad
        FROM ventas v
        INNER JOIN detalle_venta dv ON v.id = dv.id_venta
        INNER JOIN productos p ON dv.id_producto = p.id
        WHERE v.estado = 1
        GROUP BY p.id, p.nombre
        ORDER BY utilidad DESC
        """, nativeQuery = true)
    List<ReporteUtilidadProductoDTO> obtenerUtilidadPorProducto();

    @Query(value = """
        SELECT 
            p.nombre AS producto,
            SUM(dv.cantidad) AS cantidadVendida,
            SUM(dv.subtotal) AS totalVenta,
            SUM((dv.precio_unitario - COALESCE(p.precio_compra, 0)) * dv.cantidad) AS utilidad
        FROM ventas v
        INNER JOIN detalle_venta dv ON v.id = dv.id_venta
        INNER JOIN productos p ON dv.id_producto = p.id
        WHERE v.estado = 1 
        AND v.fecha BETWEEN :inicio AND :fin
        GROUP BY p.id, p.nombre
        ORDER BY utilidad DESC
        """, nativeQuery = true)
    List<ReporteUtilidadProductoDTO> obtenerUtilidadPorProductoRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
