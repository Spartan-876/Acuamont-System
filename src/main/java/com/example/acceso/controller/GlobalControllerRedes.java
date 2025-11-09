package com.example.acceso.controller;

import com.example.acceso.model.RedSocial;
import com.example.acceso.service.Implements.RedSocialServiceImpl;
import com.example.acceso.service.Interfaces.RedSocialService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Controlador de consejo global para agregar datos comunes a todos los modelos.
 *
 * Esta clase utiliza {@link ControllerAdvice} para interceptar las peticiones
 * y añadir atributos al modelo que estarán disponibles en todas las vistas
 * renderizadas por cualquier controlador. Es especialmente útil para datos que
 * se repiten en múltiples páginas, como información en el header o footer.
 */
@ControllerAdvice
public class GlobalControllerRedes {

    private final RedSocialService redSocialService;

    /**
     * Constructor para la inyección de dependencias del servicio de redes sociales.
     *
     * @param redSocialService El servicio que maneja la lógica de negocio de las redes sociales.
     */
    public GlobalControllerRedes(RedSocialService redSocialService) {
        this.redSocialService = redSocialService;
    }

    /**
     * Carga la lista de redes sociales activas y la agrega al modelo global.
     *
     * Este método se ejecuta antes de que se renderice cualquier vista y pone
     * la lista de redes sociales activas en el modelo bajo el nombre "redesSocialesActivas".
     * Esto permite acceder a dicha lista desde cualquier plantilla Thymeleaf (por ejemplo, en el footer).
     *
     * @return Una lista de objetos {@link RedSocial} que tienen el estado activo (1).
     */
    @ModelAttribute("redesSocialesActivas")
    public List<RedSocial> cargarRedesSocialesGlobales() {

        return redSocialService.listarRedesSociales()
                .stream()
                .filter(red -> red.getEstado() == 1)
                .toList();
    }

}
