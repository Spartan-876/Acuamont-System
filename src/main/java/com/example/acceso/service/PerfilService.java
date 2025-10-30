package com.example.acceso.service;

import com.example.acceso.model.Perfil;
import com.example.acceso.model.Opcion;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para gestionar la lógica de negocio de los perfiles de usuario.
 *
 * Define los métodos que deben ser implementados para las operaciones CRUD
 * (Crear, Leer, Actualizar, Eliminar) sobre las entidades de Perfil y Opcion.
 */
public interface PerfilService {
    /**
     * Obtiene una lista de todos los perfiles que están activos.
     *
     * @return Una lista de objetos {@link Perfil} con estado activo.
     */
    List<Perfil> listarPerfilesActivos();

    /**
     * Obtiene una lista de todos los perfiles, sin importar su estado.
     *
     * @return Una lista con todos los objetos {@link Perfil} en la base de datos.
     */
    List<Perfil> listarTodosLosPerfiles();

    /**
     * Guarda o actualiza un perfil en la base de datos.
     * <p>
     * Si el perfil tiene un ID, se actualiza. Si no, se crea uno nuevo.
     *
     * @param perfil El objeto {@link Perfil} a guardar o actualizar.
     * @return El perfil guardado con su ID asignado o actualizado.
     */
    Perfil guardarPerfil(Perfil perfil);

    /**
     * Busca un perfil por su ID.
     *
     * @param id El ID del perfil a buscar.
     * @return Un {@link Optional} que contiene el perfil si se encuentra, o un Optional vacío si no.
     */
    Optional<Perfil> obtenerPerfilPorId(Long id);

    /**
     * Cambia el estado de un perfil entre activo e inactivo.
     *
     * @param id El ID del perfil cuyo estado se va a cambiar.
     * @return Un {@link Optional} con el perfil actualizado si se encontró, o un Optional vacío si no.
     */
    Optional<Perfil> cambiarEstadoPerfil(Long id);

    /**
     * Obtiene una lista de todas las opciones (permisos) disponibles en el sistema.
     *
     * @return Una lista con todos los objetos {@link Opcion}.
     */
    List<Opcion> listarTodasLasOpciones();

    /**
     * Elimina un perfil de la base de datos por su ID.
     * <p>
     * <strong>Advertencia:</strong> Este método realiza un borrado físico.
     * Si el perfil está en uso por algún usuario, puede causar errores de integridad referencial.
     *
     * @param id El ID del perfil a eliminar.
     */
    void eliminarPerfil(Long id);
}