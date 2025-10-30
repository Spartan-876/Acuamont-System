package com.example.acceso.controller;

import com.example.acceso.model.Opcion;
import com.example.acceso.model.Usuario;
import com.example.acceso.service.ServicioAutenticacionDosPasos;
import com.example.acceso.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.Optional;

/**
 * Controlador para gestionar el proceso de autenticación de usuarios.
 *
 * Maneja las peticiones de inicio de sesión (login), cierre de sesión (logout)
 * y la verificación de credenciales, incluyendo la autenticación de dos
 * factores (2FA).
 */
@Controller
public class LoginController {
    private final UsuarioService usuarioService;
    private final ServicioAutenticacionDosPasos servicio2FA;

    /**
     * Constructor para la inyección de dependencias de los servicios necesarios.
     *
     * @param usuarioService El servicio para gestionar la lógica de negocio de los
     *                       usuarios.
     * @param servicio2FA    El servicio para manejar la lógica de la autenticación
     *                       de dos factores.
     */
    public LoginController(UsuarioService usuarioService, ServicioAutenticacionDosPasos servicio2FA) {
        this.usuarioService = usuarioService;
        this.servicio2FA = servicio2FA;
    }

    /**
     * Muestra el formulario de inicio de sesión.
     *
     * Si el usuario ya ha iniciado sesión (existe un atributo "usuarioLogueado" en
     * la sesión),
     * lo redirige a la página principal para evitar que vea el formulario de login
     * de nuevo.
     *
     * @param session La sesión HTTP actual.
     * @return El nombre de la vista "login" o una redirección a la raíz ("/").
     */
    @GetMapping("/login")
    public String mostrarFormularioLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/";
        }
        return "login";
    }

    /**
     * Procesa la solicitud de inicio de sesión enviada desde el formulario.
     *
     * Valida el nombre de usuario, la contraseña y el token de 2FA (si es
     * aplicable).
     * Si la autenticación es exitosa, establece los atributos necesarios en la
     * sesión
     * y redirige al dashboard. En caso de fallo, redirige de vuelta al formulario
     * de login
     * con un mensaje de error.
     *
     * @param usuario            El nombre de usuario ingresado en el formulario.
     * @param clave              La contraseña ingresada en el formulario.
     * @param token              El token de seguridad de 2FA ingresado en el
     *                           formulario.
     * @param session            La sesión HTTP para almacenar los datos del usuario
     *                           logueado.
     * @param redirectAttributes Atributos para pasar mensajes (errores) a través de
     *                           la redirección.
     * @return Una cadena de redirección a la raíz ("/") en caso de éxito, o a
     *         "/login" en caso de error.
     */
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario, @RequestParam String clave, @RequestParam String token,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioService.findByUsuario(usuario);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/login";
        }

        Usuario usuarioEncontrado = usuarioOpt.get();

        // Verifica que el usuario esté activo.
        if (usuarioEncontrado.getEstado() != 1) { // 1 = Activo
            redirectAttributes.addFlashAttribute("error", "Este usuario se encuentra inactivo.");
            return "redirect:/login";
        }

        // Verifica la contraseña.
        if (usuarioService.verificarContrasena(clave, usuarioEncontrado.getClave())) {

            // Si el usuario tiene 2FA activado, verifica el token.
            if (usuarioEncontrado.isUsa2FA()) {
                if (token == null || token.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Se requiere el token de seguridad.");
                    return "redirect:/login";
                }
                if (!servicio2FA.esCodigoValido(usuarioEncontrado.getSecreto2FA(), token)) {
                    redirectAttributes.addFlashAttribute("error", "El token de seguridad es incorrecto.");
                    return "redirect:/login";
                }
            }

            // Autenticación exitosa: guarda el usuario en la sesión.
            session.setAttribute("usuarioLogueado", usuarioEncontrado);

            // Carga las opciones del menú del perfil del usuario y las guarda en la sesión.
            var opcionesMenu = usuarioEncontrado.getPerfil().getOpciones().stream()
                    .sorted(Comparator.comparing(Opcion::getId))
                    .toList();
            session.setAttribute("menuOpciones", opcionesMenu);

            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Contraseña incorrecta.");
            return "redirect:/login";
        }
    }

    /**
     * Procesa la solicitud de cierre de sesión.
     *
     * Invalida la sesión HTTP actual, eliminando todos los atributos almacenados
     * (incluyendo "usuarioLogueado"), y redirige al formulario de login con un
     * mensaje de confirmación.
     *
     * @param session            La sesión HTTP a invalidar.
     * @param redirectAttributes Atributos para pasar el mensaje de cierre de
     *                           sesión.
     * @return Una cadena de redirección a "/login".
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("logout", "Has cerrado sesión exitosamente.");
        return "redirect:/login";
    }
}