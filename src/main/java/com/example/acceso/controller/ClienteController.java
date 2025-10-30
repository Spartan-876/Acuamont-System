package com.example.acceso.controller;

import com.example.acceso.model.Cliente;
import com.example.acceso.service.ClienteService;
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

/**
 * Controlador para gestionar las operaciones CRUD de los clientes.
 *
 * Proporciona endpoints para la vista de gestión de clientes y una API REST
 * para interactuar con los datos de clientes, incluyendo la consulta a una API externa
 * para obtener datos por DNI/RUC.
 */
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    /**
     * Token de autorización para la API externa de consulta de documentos.
     * Inyectado desde el archivo de propiedades.
     */
    @Value("${miapi.token}")
    private String tokenCode;

    /**
     * URL base de la API externa para consultar DNI.
     * Inyectado desde el archivo de propiedades.
     */
    @Value("${miapi.url.dni}")
    private String urlDni;

    /**
     * URL base de la API externa para consultar RUC.
     * Inyectado desde el archivo de propiedades.
     */
    @Value("${miapi.url.ruc}")
    private String urlRuc;

    private final ClienteService clienteService;

    /**
     * Constructor para la inyección de dependencias del servicio de clientes.
     *
     * @param clienteService El servicio que maneja la lógica de negocio de los clientes.
     */
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Muestra la página de gestión de clientes.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "clientes".
     */
    @GetMapping("/listar")
    public String listarClientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("formCliente", new Cliente());
        return "clientes";
    }

    /**
     * Endpoint de la API para obtener todos los clientes.
     *
     * @return Un {@link ResponseEntity} con la lista de clientes en formato JSON.
     */
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarClientesApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", clienteService.listarClientes());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de la API para guardar o actualizar un cliente.
     *
     * @param cliente El cliente a guardar, validado.
     * @param bindingResult El resultado de la validación.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarClienteApi(@Valid @RequestBody Cliente cliente, BindingResult bindingResult) {
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
            Cliente clienteGuardado = clienteService.guardarCliente(cliente);
            response.put("success", true);
            response.put("cliente", clienteGuardado);
            response.put("message",
                    cliente.getId() != null ? "Cliente actualizado correctamente" : "Cliente creado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar el cliente: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para cambiar el estado (activo/inactivo) de un cliente.
     *
     * @param id El ID del cliente cuyo estado se va a cambiar.
     * @return Un {@link ResponseEntity} con el cliente actualizado o un error si no se encuentra.
     */
    @PostMapping("/api/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoClienteAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            return clienteService.cambiarEstadoCliente(id)
                    .map(cliente -> {
                        response.put("success", true);
                        response.put("data", cliente);
                        response.put("message", "Estado del cliente actualizado correctamente");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(()->{
                        response.put("success", false);
                        response.put("message", "Cliente no encontrado");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el estado del cliente: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para obtener un cliente por su ID.
     *
     * @param id El ID del cliente a obtener.
     * @return Un {@link ResponseEntity} con el cliente encontrado o un estado 404 si no existe.
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerCliente(@PathVariable Long id) {
        try {
            return clienteService.obtenerClientePorId(id).map(cliente -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", cliente);
                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener el cliente: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para realizar el borrado lógico de un cliente.
     *
     * @param id El ID del cliente a eliminar.
     * @return Un {@link ResponseEntity} con el resultado de la operación.
     */
    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarCliente(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!clienteService.obtenerClientePorId(id).isPresent()) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            clienteService.eliminarCliente(id);
            response.put("success", true);
            response.put("message", "Cliente eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el cliente: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint de la API para buscar información de una persona o empresa por su número de documento (DNI/RUC)
     * utilizando un servicio externo.
     *
     * @param documento El número de DNI (8 dígitos) o RUC (11 dígitos).
     * @return Un {@link ResponseEntity} con los datos encontrados o un mensaje de error.
     */
    @GetMapping("/api/buscar-documento/{documento}")
    @ResponseBody
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String documento) {
        String token = tokenCode;
        String url;

        if (documento.length() == 8) {
            url = urlDni + documento;
        } else if (documento.length() == 11) {
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

    /**
     * Endpoint de la API para buscar un cliente existente en la base de datos local por su número de documento.
     *
     * @param documento El número de documento del cliente a buscar.
     * @return Un {@link ResponseEntity} con los datos del cliente si se encuentra.
     */
    @GetMapping("/api/buscar-cliente-documento/{documento}")
    public ResponseEntity<?> buscarPorDocumentoInterno(@PathVariable String documento) {
        return clienteService.obtenerClientePorDocumento(documento)
                .map(cliente -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", cliente);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Cliente no encontrado en la base de datos local.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
}
