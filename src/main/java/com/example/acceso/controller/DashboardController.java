// Define el paquete al que pertenece la clase.
package com.example.acceso.controller;

// Importaciones de clases necesarias de otros paquetes.
import com.example.acceso.service.CategoriaService;
import com.example.acceso.service.ProductoService;
import com.example.acceso.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// @Controller: Marca esta clase como un controlador de Spring MVC, encargado de manejar peticiones web.
@Controller
public class DashboardController {

    // Declara una dependencia final al servicio de usuario. 'final' asegura que se
    // inicialice en el constructor.
    private final UsuarioService usuarioService;
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    // Constructor para la inyección de dependencias. Spring automáticamente
    // proporcionará una instancia de UsuarioService.
    public DashboardController(UsuarioService usuarioService, ProductoService productoService, CategoriaService categoriaService) {
        this.usuarioService = usuarioService;
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    // @GetMapping("/"): Asocia este método a las peticiones HTTP GET para la URL
    // raíz ("/").
    // Es la página principal que se muestra después de iniciar sesión.
    @GetMapping("/")
    public String mostrarDashboard(Model model) {
        // 1. Llama al método contarUsuarios() del servicio para obtener el número total
        // de usuarios activos e inactivos (excluyendo los eliminados).
        long totalUsuarios = usuarioService.contarUsuarios();
        long totalProductos = productoService.contarProductos();
        long totalCategorias = categoriaService.contarCategorias();

        // 2. 'model' es un objeto que permite pasar datos desde el controlador a la
        // vista (HTML).
        // Aquí, añadimos el conteo de usuarios al modelo con el nombre "totalUsuarios".
        model.addAttribute("totalUsuarios", totalUsuarios);

        model.addAttribute("totalCategorias",totalCategorias);

        model.addAttribute("totalProductos",totalProductos);

        // 3. Devuelve el nombre de la vista (el archivo HTML) que se debe renderizar.
        // Spring Boot buscará un archivo llamado "index.html" en la carpeta
        // 'src/main/resources/templates'.
        return "index";
    }

    private final Path slidePath = Paths.get("slide-Inicio/");

    @GetMapping("/PrincipalPage-web")
    public String principalPage(Model model) throws IOException {

        if (!Files.exists(slidePath)) {
            Files.createDirectories(slidePath);
            model.addAttribute("slides", new ArrayList<>());
        } else {
            List<String> slides = Files.list(slidePath)
                    .filter(Files::isRegularFile)
                    .map(path -> "/slide-Inicio/" + path.getFileName().toString())
                    .collect(Collectors.toList());
            model.addAttribute("slides", slides);
        }

        return "PrincipalPage-WEB";
    }

    @GetMapping("/Contacto-web")
    public String mostrarPaginaContacto() {
        return "Contacto-WEB";
    }

    @GetMapping("/Productos-web")
    public String mostrarPaginaProductos() {
        return "Productos-WEB";
    }

    @GetMapping("/Servicios-web")
    public String mostrarPaginaServicios() {
        return "Servicios-WEB";
    }

}