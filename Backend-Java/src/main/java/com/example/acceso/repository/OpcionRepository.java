package com.example.acceso.repository;

import com.example.acceso.model.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Opcion}.
 *
 * <p>
 * Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar,
 * Eliminar)
 * en la base de datos para las opciones del sistema (permisos). Al extender
 * {@link JpaRepository},
 * se heredan automáticamente métodos como {@code save()}, {@code findById()},
 * {@code findAll()}, etc.
 * </p>
 */
@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Long> {
}