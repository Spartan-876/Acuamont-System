package com.example.acceso.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de sesiones para proteger las rutas privadas de la aplicación.
 *
 * Esta clase implementa {@link HandlerInterceptor} para interceptar todas las
 * peticiones entrantes ANTES de que lleguen al controlador (preHandle).
 *
 * Su propósito es verificar si existe una sesión de usuario válida. Si no
 * existe, la petición es redirigida a la página principal.
 *
 * @see com.example.acceso.config.WebConfig (Donde se registra este interceptor)
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    /**
     * Intercepta la petición antes de que sea manejada por el controlador.
     *
     * Verifica la existencia de una sesión y si el atributo "usuarioLogueado"
     * está presente en esa sesión.
     *
     * @param request La petición HTTP entrante.
     * @param response La respuesta HTTP (usada para redirigir si es necesario).
     * @param handler El manejador (controlador) que se ejecutaría.
     * @return {@code true} si la sesión es válida y la petición puede continuar
     * hacia el controlador.
     * {@code false} si la sesión es inválida (nula o sin atributo),
     * en cuyo caso se redirige al usuario a "/PrincipalPage-web" y
     * se detiene el procesamiento de la petición.
     * @throws Exception Si ocurre un error durante el envío de la redirección.
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect("/PrincipalPage-web");

            return false;
        }

        return true;
    }
}