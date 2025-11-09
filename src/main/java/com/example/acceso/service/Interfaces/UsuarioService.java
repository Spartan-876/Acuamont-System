package com.example.acceso.service.Interfaces;

import com.example.acceso.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> listarUsuarios();

    Usuario guardarUsuario(Usuario usuario);

    long contarUsuarios();

    Optional<Usuario> obtenerUsuarioPorId(Long id);

    Optional<Usuario> findByUsuario(String usuario);

    void eliminarUsuario(Long id);

    Optional<Usuario> cambiarEstadoUsuario(Long id);

    boolean existeUsuario(String nombreUsuario);

    boolean existeCorreo(String correo);

    boolean verificarContrasena(String contrasenaTextoPlano, String contrasenaEncriptada);

    void activar2FA(Long idUsuario, String secreto2FA);

}
