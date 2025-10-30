package com.example.acceso.repository;

import com.example.acceso.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Perfil}.
 *
 * <p>
 * Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar,
 * Eliminar)
 * en la base de datos para los perfiles de usuario, así como consultas
 * personalizadas derivadas
 * de los nombres de los métodos.
 * </p>
 */
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    /**
     * Busca todos los perfiles cuyo campo 'estado' es verdadero.
     * <p>
     * Este método se basa en la convención de nombres de Spring Data JPA para
     * generar
     * la consulta automáticamente.
     * </p>
     *
     * @return Una lista de objetos {@link Perfil} que están activos.
     */
    List<Perfil> findByEstadoTrue();
}