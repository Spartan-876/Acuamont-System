package com.example.acceso.services;


import com.example.acceso.model.Usuario;
import com.example.acceso.repository.UsuarioRepository;
import com.example.acceso.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test de servicio de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioBase = new Usuario();
        usuarioBase.setId(1L);
        usuarioBase.setNombre("John");
        usuarioBase.setUsuario("john");
        usuarioBase.setCorreo("john@example.com");
        usuarioBase.setClave("12345");
        usuarioBase.setEstado(1);
    }

    @Test
    void guardarUsuario_nuevoUsuario_debeGuardarConClaveEncriptada() {
        // given
        usuarioBase.setId(null);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // when
        Usuario guardado = usuarioService.guardarUsuario(usuarioBase);

        // then
        assertNotNull(guardado.getId());
        assertTrue(guardado.getClave().startsWith("$2a$")); // indica BCrypt
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void guardarUsuario_actualizaUsuario_sinNuevaClave_mantieneClaveAnterior() {
        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setClave("$2a$encodedpassword");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any())).thenReturn(usuarioBase);

        // given: usuario con mismo ID pero sin clave
        usuarioBase.setId(1L);
        usuarioBase.setClave(null);

        // when
        Usuario resultado = usuarioService.guardarUsuario(usuarioBase);

        // then
        assertEquals("$2a$encodedpassword", resultado.getClave());
        verify(usuarioRepository).save(any());
    }

    @Test
    void guardarUsuario_errorNombreVacio_lanzaExcepcion() {
        usuarioBase.setNombre("");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                usuarioService.guardarUsuario(usuarioBase)
        );

        assertTrue(ex.getMessage().contains("El nombre es obligatorio"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void eliminarUsuario_idInvalido_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> usuarioService.eliminarUsuario(null));
        assertThrows(RuntimeException.class, () -> usuarioService.eliminarUsuario(0L));
    }

    @Test
    void eliminarUsuario_valido_cambiaEstadoA2() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

        usuarioService.eliminarUsuario(1L);

        assertEquals(2, usuarioBase.getEstado());
        verify(usuarioRepository).save(usuarioBase);
    }

    @Test
    void existeUsuario_retornaTrueSiExiste() {
        when(usuarioRepository.existsByUsuario("john")).thenReturn(true);

        boolean existe = usuarioService.existeUsuario("john");

        assertTrue(existe);
        verify(usuarioRepository).existsByUsuario("john");
    }

    @Test
    void verificarContrasena_validaCorrectamente() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("12345");

        assertTrue(usuarioService.verificarContrasena("12345", encoded));
        assertFalse(usuarioService.verificarContrasena("1234", encoded));
    }

    @Test
    void guardarUsuario_restriccionDuplicadaCorreo_lanzaExcepcion() {
        usuarioBase.setId(null);

        when(usuarioRepository.save(any())).thenThrow(new DataIntegrityViolationException("correo"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                usuarioService.guardarUsuario(usuarioBase)
        );

        assertTrue(ex.getMessage().contains("El correo electrónico ya está registrado"));
    }


}

