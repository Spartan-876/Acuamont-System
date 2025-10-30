package com.example.acceso.controller;

import com.example.acceso.service.CategoriaService;
import com.example.acceso.service.ProductoService;
import com.example.acceso.service.UsuarioService;
import com.example.acceso.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador principal para el dashboard y las páginas públicas del sitio web.
 *
 * Gestiona las peticiones a la página de inicio de la aplicación (dashboard)
 * tras el inicio de sesión, y a las páginas de la parte pública como la página
 * principal, contacto, productos y servicios.
 */
@Controller
public class DashboardController {

    private final UsuarioService usuarioService;
    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final VentaService ventaService;

    /**
     * Constructor para la inyección de dependencias de los servicios necesarios.
     *
     * @param usuarioService   Servicio para gestionar la lógica de negocio de los usuarios.
     * @param productoService  Servicio para gestionar la lógica de negocio de los productos.
     * @param categoriaService Servicio para gestionar la lógica de negocio de las categorías.
     * @param ventaService     Servicio para gestionar la lógica de negocio de las ventas.
     */
    public DashboardController(UsuarioService usuarioService, ProductoService productoService, CategoriaService categoriaService, VentaService ventaService) {
        this.usuarioService = usuarioService;
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.ventaService = ventaService;
    }

    /**
     * Muestra el dashboard principal de la aplicación después del inicio de sesión.
     *
     * Recopila estadísticas clave como el total de usuarios, productos, categorías,
     * ventas del día, ventas del mes y deudas totales, y las pasa a la vista.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "index" que corresponde al dashboard.
     */
    @GetMapping("/")
    public String mostrarDashboard(Model model) {
        long totalUsuarios = usuarioService.contarUsuarios();
        long totalProductos = productoService.contarProductos();
        long totalCategorias = categoriaService.contarCategorias();
        BigDecimal totalVentasDia = ventaService.totalVentasDelDia();
        BigDecimal totalVentasMes = ventaService.totalVentasDelMes();
        BigDecimal totalDeuda = ventaService.totalDeuda();

        model.addAttribute("totalUsuarios", totalUsuarios);

        model.addAttribute("totalCategorias",totalCategorias);

        model.addAttribute("totalProductos",totalProductos);

        model.addAttribute("totalVentasDia",totalVentasDia);

        model.addAttribute("totalVentasMes",totalVentasMes);

        model.addAttribute("totalDeuda",totalDeuda);

        return "index";
    }

    private final Path slidePath = Paths.get("slide-Inicio/");

    /**
     * Muestra la página principal pública del sitio web.
     *
     * Carga la lista de imágenes para el carrusel (slides) y las pasa a la vista.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "PrincipalPage-WEB".
     * @throws IOException Si ocurre un error al leer el directorio de slides.
     */
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

    /**
     * Muestra la página pública de "Contacto".
     *
     * @return El nombre de la vista "Contacto-WEB".
     */
    @GetMapping("/Contacto-web")
    public String mostrarPaginaContacto() {
        return "Contacto-WEB";
    }

    /**
     * Muestra la página pública de "Productos".
     *
     * @return El nombre de la vista "Productos-WEB".
     */
    @GetMapping("/Productos-web")
    public String mostrarPaginaProductos() {
        return "Productos-WEB";
    }

    /**
     * Muestra la página pública de "Servicios".
     *
     * @return El nombre de la vista "Servicios-WEB".
     */
    @GetMapping("/Servicios-web")
    public String mostrarPaginaServicios() {
        return "Servicios-WEB";
    }

}