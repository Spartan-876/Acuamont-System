package com.example.acceso.repository;

import com.example.acceso.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Usuario}.
 *
 * <p>
 * Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar,
 * Eliminar)
 * en la base de datos para los usuarios, así como consultas personalizadas
 * derivadas
 * de los nombres de los métodos para buscar y verificar la existencia de
 * usuarios.
 * </p>
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario (login).
     *
     * @param usuario El nombre de usuario a buscar.
     * @return Un {@link Optional} que contiene el usuario si se encuentra, o un
     *         Optional vacío si no.
     */
    Optional<Usuario> findByUsuario(String usuario);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param correo El correo electrónico a buscar.
     * @return Un {@link Optional} que contiene el usuario si se encuentra, o un
     *         Optional vacío si no.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si existe un usuario con un nombre de usuario específico.
     * <p>
     * Este método es más eficiente que {@code findByUsuario().isPresent()} si solo
     * se necesita
     * saber si el usuario existe.
     * </p>
     *
     * @param usuario El nombre de usuario a verificar.
     * @return {@code true} si el usuario existe, {@code false} en caso contrario.
     */
    boolean existsByUsuario(String usuario);

    /**
     * Verifica si existe un usuario con una dirección de correo electrónico
     * específica.
     * <p>
     * Este método es más eficiente que {@code findByCorreo().isPresent()} si solo
     * se necesita
     * saber si el correo existe.
     * </p>
     *
     * @param correo El correo electrónico a verificar.
     * @return {@code true} si el correo ya está registrado, {@code false} en caso
     *         contrario.
     */
    boolean existsByCorreo(String correo);

    /**
     * Obtiene una lista de todos los usuarios cuyo estado no coincide con el valor
     * proporcionado.
     * <p>
     * Se utiliza comúnmente para excluir los usuarios eliminados lógicamente
     * (estado = 2).
     * </p>
     *
     * @param estado El estado a excluir.
     * @return Una lista de objetos {@link Usuario}.
     */
    List<Usuario> findAllByEstadoNot(Integer estado);

    /**
     * Cuenta el número de usuarios cuyo estado no coincide con el valor
     * proporcionado.
     *
     * @param estado El estado a excluir en el conteo.
     * @return El número de usuarios que no tienen el estado especificado.
     */
    long countByEstadoNot(Integer estado);
}