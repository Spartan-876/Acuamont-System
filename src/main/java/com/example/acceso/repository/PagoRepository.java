package com.example.acceso.repository;

import com.example.acceso.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("SELECT p FROM Pago p WHERE p.cuota.venta.id = :ventaId")
    List<Pago> findPagosByVentaId(@Param("ventaId") Long ventaId);

}
