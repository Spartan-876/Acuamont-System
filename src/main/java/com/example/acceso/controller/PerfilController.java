package com.example.acceso.controller;

import com.example.acceso.model.Perfil;
import com.example.acceso.service.PerfilService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar las operaciones CRUD de los perfiles de usuario.
 *
 * Proporciona endpoints para la vista de gestión de perfiles y una API REST
 * para interactuar con los datos de perfiles, incluyendo la asignación de
 * opciones (permisos).
 */
@Controller
@RequestMapping("/perfiles")
public class PerfilController {

    private final PerfilService perfilService;

    /**
     * Constructor para la inyección de dependencias del servicio de perfiles.
     *
     * @param perfilService El servicio que maneja la lógica de negocio de los perfiles.
     */
    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * Muestra la página de gestión de perfiles.
     *
     * @return El nombre de la vista "perfiles".
     */
    @GetMapping("/listar")
    public String mostrarPaginaPerfiles() {
        return "perfiles"; // Devuelve el nombre de la vista (perfiles.html)
    }

    /**
     * Endpoint de la API para obtener todos los perfiles.
     *
     * @return Un {@link ResponseEntity} con la lista de todos los perfiles en formato JSON.
     */
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarPerfilesApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", perfilService.listarTodosLosPerfiles());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para obtener un perfil por su ID.
     *
     * @param id El ID del perfil a obtener.
     * @return Un {@link ResponseEntity} con los datos del perfil encontrado o un estado 404 si no existe.
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerPerfil(@PathVariable Long id) {
        return perfilService.obtenerPerfilPorId(id)
                .map(perfil -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    // Preparamos los datos para el frontend
                    Map<String, Object> perfilData = new HashMap<>();
                    perfilData.put("id", perfil.getId());
                    perfilData.put("nombre", perfil.getNombre());
                    perfilData.put("descripcion", perfil.getDescripcion());
                    perfilData.put("estado", perfil.isEstado());
                    perfilData.put("opciones",
                            perfil.getOpciones().stream().map(op -> op.getId()).collect(Collectors.toSet()));

                    response.put("data", perfilData);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint de la API para guardar o actualizar un perfil.
     *
     * @param perfil El objeto {@link Perfil} a guardar, recibido en el cuerpo de la petición.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarPerfil(@RequestBody Perfil perfil) {
        Map<String, Object> response = new HashMap<>();
        try {
            Perfil perfilGuardado = perfilService.guardarPerfil(perfil);
            response.put("success", true);
            response.put("message", perfil.getId() != null ? "Perfil actualizado" : "Perfil creado");
            response.put("perfil", perfilGuardado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar el perfil: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para cambiar el estado (activo/inactivo) de un perfil.
     *
     * @param id El ID del perfil cuyo estado se va a cambiar.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @PostMapping("/api/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoPerfil(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return perfilService.cambiarEstadoPerfil(id)
                .map(perfil -> {
                    response.put("success", true);
                    response.put("message", "Estado del perfil actualizado");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Perfil no encontrado");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    /**
     * Endpoint de la API para obtener todas las opciones (permisos) disponibles.
     *
     * @return Un {@link ResponseEntity} con la lista de todas las opciones.
     */
    @GetMapping("/api/opciones")
    @ResponseBody
    public ResponseEntity<?> listarOpcionesApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", perfilService.listarTodasLasOpciones());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para eliminar un perfil.
     *
     * @param id El ID del perfil a eliminar.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarPerfil(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Opcional: Añadir validación para no poder eliminar perfiles en uso.
            // if (usuarioService.existeUsuarioConPerfil(id)) {
            // response.put("success", false);
            // response.put("message", "No se puede eliminar el perfil porque está asignado a uno o más usuarios.");
            // return ResponseEntity.badRequest().body(response);
            // }
            perfilService.eliminarPerfil(id);
            response.put("success", true);
            response.put("message", "Perfil eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el perfil: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}