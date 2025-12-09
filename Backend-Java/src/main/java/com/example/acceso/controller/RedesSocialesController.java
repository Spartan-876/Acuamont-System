package com.example.acceso.controller;

import com.example.acceso.DTO.RedSocialDTO;
import com.example.acceso.model.RedSocial;
import com.example.acceso.service.Interfaces.RedSocialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones de las redes sociales de la empresa.
 *
 * Proporciona una API REST para listar, actualizar y cambiar el estado de las
 * redes sociales que se muestran en el sitio web.
 */
@Controller
@RequestMapping("/redes")
public class RedesSocialesController {

    private final RedSocialService redSocialService;

    /**
     * Constructor para la inyección de dependencias del servicio de redes sociales.
     *
     * @param redSocialService El servicio que maneja la lógica de negocio de las redes sociales.
     */
    public RedesSocialesController(RedSocialService redSocialService) {
        this.redSocialService = redSocialService;
    }

    /**
     * Endpoint de la API para obtener todas las redes sociales.
     *
     * @return Un {@link ResponseEntity} con la lista de todas las redes sociales en formato JSON.
     */
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarRedesSocialesApi() {
        Map<String, Object> response = new HashMap<>();
        List<RedSocial> redesSociales = redSocialService.listarRedesSociales();
        response.put("success", true);
        response.put("data", redesSociales);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para actualizar la URL de una red social.
     *
     * @param id El ID de la red social a actualizar.
     * @param redSocial El DTO con la nueva URL.
     * @return Un {@link ResponseEntity} con la red social actualizada.
     */
    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarRedSocial(@PathVariable Long id, @RequestBody RedSocialDTO redSocial) {
        RedSocial redSocialActualizada = redSocialService.actualizarRedSocial(id, redSocial);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Red social actualizada correctamente");
        response.put("data", redSocialActualizada);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para cambiar el estado (activo/inactivo) de una red social.
     *
     * @param id El ID de la red social cuyo estado se va a cambiar.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @PostMapping("/api/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoRedSocial(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return redSocialService.cambiarEstadoRedSocial(id)
                .map(redSocial -> {
                    response.put("success", true);
                    response.put("message", "Estado de la red social actualizado correctamente");
                    response.put("data", redSocial);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Red social no encontrada");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }


}
