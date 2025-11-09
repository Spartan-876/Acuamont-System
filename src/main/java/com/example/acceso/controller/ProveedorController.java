package com.example.acceso.controller;

import com.example.acceso.model.Proveedor;
import com.example.acceso.service.Implements.ProveedorServiceImpl;
import com.example.acceso.service.Interfaces.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {
    
    @Value("${miapi.token}")
    private String tokenCode;

    @Value("${miapi.url.ruc}")
    private String urlRuc;

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }
    
    @GetMapping("/listar")
    public String listarProveedores(Model model) {
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("formProveedor", new Proveedor());
        return "proveedores";
    }
    
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarProveedoresApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", proveedorService.listarProveedores());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarProveedorApi(@Valid @RequestBody Proveedor proveedor, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if(bindingResult.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            response.put("success", false);
            response.put("message", "Datos inválidos");
            response.put("errors", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Proveedor proveedorGuardado = proveedorService.guardarProveedor(proveedor);
            response.put("success", true);
            response.put("proveedor", proveedorGuardado);
            response.put("message",
                    proveedor.getId() != null ? "Proveedor actualizado correctamente" : "Proveedor creado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/api/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoProveedorAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            return proveedorService.cambiarEstadoProveedor(id)
                    .map(proveedor -> {
                        response.put("success", true);
                        response.put("data", proveedor);
                        response.put("message", "Estado del proveedor actualizado correctamente");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(()->{
                        response.put("success", false);
                        response.put("message", "Proveedor no encontrado");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el estado del proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerProveedor(@PathVariable Long id) {
        try {
            return proveedorService.obtenerProveedorPorId(id).map(proveedor -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", proveedor);
                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarProveedor(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!proveedorService.obtenerProveedorPorId(id).isPresent()) {
                response.put("success", false);
                response.put("message", "Proveedor no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            proveedorService.eliminarProveedor(id);
            response.put("success", true);
            response.put("message", "Proveedor eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el proveedor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/buscar-documento/{documento}")
    @ResponseBody
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String documento) {
        String token = tokenCode;
        String url;

        if (documento.length() == 11) {
            url = urlRuc + documento;
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Documento inválido"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.get("success"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "No se encontró información para el documento ingresado"));
            }

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al consultar el documento: " + e.getMessage()));
        }
    }

    @GetMapping("/api/buscar-proveedor-documento/{documento}")
    public ResponseEntity<?> buscarPorDocumentoInterno(@PathVariable String documento) {
        return proveedorService.obtenerProveedorPorDocumento(documento)
                .map(proveedor -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", proveedor);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Proveedor no encontrado en la base de datos local.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
}
