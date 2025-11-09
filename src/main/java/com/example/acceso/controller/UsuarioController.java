package com.example.acceso.controller;

import com.example.acceso.model.Usuario;
import com.example.acceso.service.Interfaces.PerfilService;
import com.example.acceso.service.Implements.ServicioAutenticacionDosPasosImpl;
import com.example.acceso.service.Implements.UsuarioServiceImpl;
import com.example.acceso.service.Interfaces.ServicioAutenticacionDosPasos;
import com.example.acceso.service.Interfaces.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestionar las operaciones CRUD de los usuarios.
 *
 * Proporciona endpoints para la vista de gestión de usuarios y una API REST
 * para interactuar con los datos de usuarios, incluyendo la asignación de perfiles
 * y la configuración de la autenticación de dos factores (2FA).
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final ServicioAutenticacionDosPasos servicio2FA;

    /**
     * Constructor para la inyección de dependencias de los servicios necesarios.
     *
     * @param usuarioService El servicio que maneja la lógica de negocio de los usuarios.
     * @param perfilService  El servicio que maneja la lógica de negocio de los perfiles.
     * @param servicio2FA    El servicio para manejar la lógica de la autenticación de dos factores.
     */
    public UsuarioController(UsuarioService usuarioService, PerfilService perfilService, ServicioAutenticacionDosPasos servicio2FA) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
        this.servicio2FA = servicio2FA;
    }

    /**
     * Muestra la página de gestión de usuarios.
     *
     * Aunque el listado principal de datos se realiza a través de la API, este método
     * prepara el modelo inicial y la estructura de la página.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "usuarios".
     */
    @GetMapping("/listar")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("formUsuario", new Usuario());
        return "usuarios";
    }

    /**
     * Endpoint de la API para obtener todos los usuarios (excluyendo eliminados).
     *
     * @return Un {@link ResponseEntity} con la lista de usuarios en formato JSON.
     */
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarUsuariosApi() {
        Map<String, Object> response = new HashMap<>();
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        response.put("success", true);
        response.put("data", usuarios);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener la lista de usuarios y el ID del usuario actualmente logueado.
     * Útil para identificar al usuario actual en las listas del frontend.
     *
     * @param session La sesión HTTP actual para obtener el usuario logueado.
     * @return Un {@link ResponseEntity} con la lista de usuarios y el ID del usuario actual.
     */
    @GetMapping("/api/usuarioLogueado")
    @ResponseBody
    public ResponseEntity<?> usuarioLogueado(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", usuarioService.listarUsuarios());

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null) {
            response.put("usuarioActual", usuarioLogueado.getId());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener todos los perfiles activos.
     *
     * @return Un {@link ResponseEntity} con la lista de perfiles activos.
     */
    @GetMapping("/api/perfiles")
    @ResponseBody
    public ResponseEntity<?> listarPerfilesActivosApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", perfilService.listarPerfilesActivos());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para guardar o actualizar un usuario.
     *
     * @param usuario       El objeto {@link Usuario} a guardar, recibido y validado desde el cuerpo de la petición.
     * @param bindingResult El resultado de la validación de los campos del usuario.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarUsuarioAjax(@Valid @RequestBody Usuario usuario, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            response.put("success", false);
            response.put("message", "Datos inválidos");
            response.put("errors", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);
            response.put("success", true);
            response.put("usuario", usuarioGuardado);
            response.put("message",
                    usuario.getId() != null ? "Usuario actualizado correctamente" : "Usuario creado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para obtener un usuario por su ID.
     *
     * @param id El ID del usuario a obtener.
     * @return Un {@link ResponseEntity} con los datos del usuario o un estado 404 si no se encuentra.
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        try {
            return usuarioService.obtenerUsuarioPorId(id).map(usuario -> {
                // Si el usuario se encuentra, lo envuelve en una respuesta exitosa.
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", usuario);
                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para realizar el borrado lógico de un usuario.
     *
     * @param id El ID del usuario a eliminar.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!usuarioService.obtenerUsuarioPorId(id).isPresent()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            usuarioService.eliminarUsuario(id);
            response.put("success", true);
            response.put("message", "Usuario eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para cambiar el estado (activo/inactivo) de un usuario.
     *
     * @param id El ID del usuario cuyo estado se va a cambiar.
     * @return Un {@link ResponseEntity} con el usuario actualizado o un error si no se encuentra.
     */
    @PostMapping("/api/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoUsuarioAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            return usuarioService.cambiarEstadoUsuario(id)
                    .map(usuario -> {
                        response.put("success", true);
                        response.put("usuario", usuario);
                        response.put("message", "Estado del usuario actualizado correctamente");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("success", false);
                        response.put("message", "Usuario no encontrado");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para generar un nuevo secreto y el URI del código QR para la configuración de 2FA.
     *
     * @param id El ID del usuario para el cual se genera el secreto 2FA.
     * @return Un {@link ResponseEntity} que contiene el secreto en texto plano y el URI de datos
     *         para generar el código QR en el frontend.
     */
    @GetMapping("/api/generar-2fa/{id}")
    @ResponseBody
    public ResponseEntity<?> generarSecreto2FA(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);

        if (usuarioOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Usuario usuario = usuarioOpt.get();
        String secreto = servicio2FA.generarNuevoSecreto();
        String qrCodeUri = servicio2FA.generarUriDatosQr(secreto, usuario.getCorreo(), "AccesoApp");

        response.put("success", true);
        response.put("secreto", secreto); // Se envía al front para mostrarlo por si no se puede escanear el QR
        response.put("qrCodeUri", qrCodeUri);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para verificar el código 2FA y activar la funcionalidad para el usuario.
     *
     * Recibe el ID del usuario, el secreto generado en el paso anterior y el código de 6 dígitos
     * ingresado por el usuario desde su aplicación de autenticación.
     *
     * @param payload Un mapa que debe contener "id", "codigo" y "secreto".
     * @return Un {@link ResponseEntity} confirmando la activación o indicando un error.
     */
    @PostMapping("/api/verificar-2fa")
    @ResponseBody
    public ResponseEntity<?> verificarYActivar2FA(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        Long id = Long.parseLong(payload.get("id"));
        String codigo = payload.get("codigo");
        String secreto = payload.get("secreto"); // El secreto se genera en el paso anterior

        if (!servicio2FA.esCodigoValido(secreto, codigo)) {
            response.put("success", false);
            response.put("message", "El código de verificación es incorrecto.");
            return ResponseEntity.badRequest().body(response);
        }

        usuarioService.activar2FA(id, secreto);

        response.put("success", true);
        response.put("message", "¡Autenticación de dos pasos activada correctamente!");
        return ResponseEntity.ok(response);
    }
}