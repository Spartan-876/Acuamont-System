package com.example.acceso.repository;

import com.example.acceso.model.AjusteInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AjusteInventarioRepository extends JpaRepository<AjusteInventario, Long> {

    @Query("SELECT ai FROM AjusteInventario ai WHERE ai.producto.id = :productoId order by ai.fecha desc")
    List<AjusteInventario> findByProductoId(Long productoId);
}
