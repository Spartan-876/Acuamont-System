package com.example.acceso.service;

import com.example.acceso.model.Usuario;
import com.example.acceso.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de los usuarios.
 *
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * sobre las entidades de Usuario, manejando la lógica de negocio como
 * validaciones, encriptación de contraseñas y transacciones.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor para la inyección de dependencias del repositorio de usuarios.
     * Inicializa el codificador de contraseñas BCrypt.
     *
     * @param usuarioRepository El repositorio para las operaciones de base de datos
     *                          de Usuario.
     */
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Obtiene una lista de todos los usuarios que no están eliminados lógicamente.
     *
     * @return Una lista de objetos {@link Usuario}.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllByEstadoNot(2);
    }

    /**
     * Guarda o actualiza un usuario en la base de datos.
     * <p>
     * Si el usuario tiene un ID, se actualiza. Si no, se crea uno nuevo.
     * Realiza validaciones, normaliza los datos (trim, toLowerCase) y encripta la
     * contraseña.
     *
     * @param usuario El objeto {@link Usuario} a guardar.
     * @return El usuario guardado con su ID asignado o actualizado.
     * @throws IllegalArgumentException Si faltan campos obligatorios, si ya existe
     *                                  un usuario/correo,
     *                                  o si ocurre otro error de integridad de
     *                                  datos.
     * @throws RuntimeException         Si ocurre un error inesperado durante el
     *                                  proceso.
     */
    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        try {
            // Validaciones adicionales
            if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            if (usuario.getUsuario() == null || usuario.getUsuario().trim().isEmpty()) {
                throw new IllegalArgumentException("El usuario es obligatorio");
            }

            if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
                throw new IllegalArgumentException("El correo es obligatorio");
            }

            // Normalizar datos
            usuario.setNombre(usuario.getNombre().trim());
            usuario.setUsuario(usuario.getUsuario().trim().toLowerCase());
            usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());

            // Manejo de contraseñas
            if (usuario.getId() != null) {
                // Usuario existente - actualización
                Optional<Usuario> usuarioExistente = obtenerUsuarioPorId(usuario.getId());
                if (usuarioExistente.isPresent()) {
                    // Si no se proporciona nueva contraseña, mantener la actual
                    if (usuario.getClave() == null || usuario.getClave().trim().isEmpty()) {
                        usuario.setClave(usuarioExistente.get().getClave());
                    } else {
                        // Encriptar nueva contraseña
                        usuario.setClave(passwordEncoder.encode(usuario.getClave().trim()));
                    }
                } else {
                    throw new IllegalArgumentException("Usuario no encontrado para actualizar");
                }
            } else {
                // Nuevo usuario
                if (usuario.getClave() == null || usuario.getClave().trim().isEmpty()) {
                    throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios");
                }
                // Encriptar contraseña
                usuario.setClave(passwordEncoder.encode(usuario.getClave().trim()));
                // Asignar estado activo por defecto a nuevos usuarios
                usuario.setEstado(1);
            }

            return usuarioRepository.save(usuario);

        } catch (DataIntegrityViolationException e) {
            // Manejar violaciones de restricciones únicas
            String message = e.getMessage().toLowerCase();
            if (message.contains("usuario")) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            } else if (message.contains("correo") || message.contains("email")) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado");
            } else {
                throw new IllegalArgumentException("Error de integridad de datos");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Cuenta el número total de usuarios que no están eliminados lógicamente.
     *
     * @return El número de usuarios activos e inactivos.
     */
    @Transactional(readOnly = true)
    public long contarUsuarios() {
        return usuarioRepository.countByEstadoNot(2);
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id El ID del usuario a buscar.
     * @return Un {@link Optional} que contiene el usuario si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su nombre de usuario (login).
     * La búsqueda no distingue mayúsculas de minúsculas.
     *
     * @param usuario El nombre de usuario a buscar.
     * @return Un {@link Optional} que contiene el usuario si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findByUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario.trim().toLowerCase());
    }

    /**
     * Realiza el borrado lógico de un usuario, cambiando su estado a 2.
     *
     * @param id El ID del usuario a eliminar.
     * @throws IllegalArgumentException si el ID es inválido o el usuario no se
     *                                  encuentra.
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }

        // Borrado lógico: cambiamos el estado a 2
        Usuario usuario = obtenerUsuarioPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEstado(2); // 2 significa "eliminado"
        usuarioRepository.save(usuario);
    }

    /**
     * Cambia el estado de un usuario entre activo (1) e inactivo (0).
     * <p>
     * Si el usuario está eliminado (estado 2), no se realiza ningún cambio.
     *
     * @param id El ID del usuario cuyo estado se va a cambiar.
     * @return Un {@link Optional} con el usuario actualizado si se encontró, o un
     *         Optional vacío si no.
     */
    @Transactional
    public Optional<Usuario> cambiarEstadoUsuario(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return obtenerUsuarioPorId(id).map(usuario -> {
            // Solo alterna entre 0 (inactivo) y 1 (activo)
            if (usuario.getEstado() == 1) {
                usuario.setEstado(0); // Desactivar
            } else if (usuario.getEstado() == 0) {
                usuario.setEstado(1); // Activar
            }
            // No se hace nada si el estado es 2 (eliminado)
            return usuarioRepository.save(usuario);
        });
    }

    /**
     * Verifica si ya existe un usuario con un nombre de usuario específico.
     *
     * @param nombreUsuario El nombre de usuario a verificar.
     * @return {@code true} si el nombre de usuario ya existe, {@code false} en caso
     *         contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return false;
        }
        return usuarioRepository.existsByUsuario(nombreUsuario.trim().toLowerCase());
    }

    /**
     * Verifica si ya existe un usuario con un correo electrónico específico.
     *
     * @param correo El correo electrónico a verificar.
     * @return {@code true} si el correo ya existe, {@code false} en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        return usuarioRepository.existsByCorreo(correo.trim().toLowerCase());
    }

    /**
     * Compara una contraseña en texto plano con una contraseña encriptada usando
     * BCrypt.
     *
     * @param contrasenaTextoPlano La contraseña ingresada por el usuario.
     * @param contrasenaEncriptada La contraseña almacenada en la base de datos.
     * @return {@code true} si las contraseñas coinciden, {@code false} en caso
     *         contrario.
     */
    public boolean verificarContrasena(String contrasenaTextoPlano, String contrasenaEncriptada) {
        return passwordEncoder.matches(contrasenaTextoPlano, contrasenaEncriptada);
    }

    /**
     * Activa la autenticación de dos factores (2FA) para un usuario.
     * <p>
     * Guarda el secreto 2FA en la base de datos y establece la bandera `usa2FA` en
     * `true`.
     *
     * @param idUsuario  El ID del usuario para el cual se activará la 2FA.
     * @param secreto2FA El secreto en formato Base32 que se compartirá con la app
     *                   de autenticación.
     * @throws IllegalArgumentException si no se encuentra un usuario con el ID
     *                                  proporcionado.
     */
    @Transactional
    public void activar2FA(Long idUsuario, String secreto2FA) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));

        usuario.setSecreto2FA(secreto2FA);
        usuario.setUsa2FA(true);
        usuarioRepository.save(usuario);
    }
}