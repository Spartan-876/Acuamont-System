package com.example.acceso.repository;

import com.example.acceso.model.AjusteInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link AjusteInventario}.
 *
 * <p>Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en la base de datos para los ajustes de inventario, así como consultas personalizadas.</p>
 */
@Repository
public interface AjusteInventarioRepository extends JpaRepository<AjusteInventario, Long> {

    /**
     * Busca todos los ajustes de inventario para un producto específico, ordenados por fecha descendente.
     *
     * @param productoId El ID del producto para el cual se buscan los ajustes.
     * @return Una lista de objetos {@link AjusteInventario} asociados al producto,
     *         ordenados del más reciente al más antiguo.
     */
    @Query("SELECT ai FROM AjusteInventario ai WHERE ai.producto.id = :productoId order by ai.fecha desc")
    List<AjusteInventario> findByProductoId(Long productoId);
}
