package com.example.acceso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar las imágenes del carrusel (slides) de la página principal.
 *
 * Proporciona endpoints para listar, guardar y eliminar las imágenes que se muestran
 * en el carrusel de la página pública.
 */
@Controller
@RequestMapping("/slides")
public class SlidesController {

    /**
     * Ruta del directorio donde se almacenan las imágenes del carrusel.
     */
    private final Path slidePath = Paths.get("slide-Inicio/");

    /**
     * Obtiene una lista de las rutas de las imágenes del carrusel.
     *
     * Si el directorio no existe, lo crea.
     *
     * @return Una lista de cadenas con las rutas de las imágenes.
     * @throws IOException Si ocurre un error de I/O al leer el directorio.
     */
    private List<String> obtenerSlides() throws IOException {
        if (!Files.exists(slidePath)) {
            Files.createDirectories(slidePath);
            return new ArrayList<>();
        }

        return Files.list(slidePath)
                .filter(Files::isRegularFile)
                .map(path -> "/slide-Inicio/" + path.getFileName().toString())
                .collect(Collectors.toList());
    }

    /**
     * Calcula el siguiente número secuencial para un nuevo slide.
     *
     * Busca el número más alto en los nombres de archivo existentes (ej. "slide3.jpg")
     * y devuelve el siguiente número.
     *
     * @return El próximo número entero para nombrar un slide.
     * @throws IOException Si ocurre un error de I/O al leer el directorio.
     */
    private int obtenerProximoNumeroSlide() throws IOException {
        if (!Files.exists(slidePath)) {
            return 1;
        }

        return Files.list(slidePath)
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .filter(name -> name.startsWith("slide"))
                .map(name -> {
                    try {
                        String numeroStr = name.replaceFirst("slide", "").replaceFirst("\\..*$", "");
                        return Integer.parseInt(numeroStr);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    /**
     * Extrae la extensión de un nombre de archivo.
     *
     * @param fileName El nombre completo del archivo (ej. "imagen.png").
     * @return La extensión con el punto (ej. ".png") o una cadena vacía si no tiene extensión.
     */
    private String obtenerExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * Muestra la página de gestión de slides.
     *
     * Carga la lista de slides existentes y la pasa a la vista.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "slides".
     * @throws IOException Si ocurre un error al obtener la lista de slides.
     */
    @GetMapping("/listar")
    public String listarSlides(Model model) throws IOException {
        model.addAttribute("slides", obtenerSlides());
        return "slides";
    }

    /**
     * Muestra la página principal pública con los slides.
     *
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la vista "principal-page".
     * @throws IOException Si ocurre un error al obtener la lista de slides.
     */
    @GetMapping("/PrincipalPage-web")
    public String paginaPrincipal(Model model) throws IOException {
        model.addAttribute("slides", obtenerSlides());
        return "principal-page";
    }

    /**
     * Guarda una nueva imagen de slide.
     *
     * @param file El archivo de imagen subido desde el formulario.
     * @param redirectAttributes Atributos para pasar mensajes a través de la redirección.
     * @return Una cadena de redirección a la página de listado de slides.
     * @throws IOException Si ocurre un error durante la operación de guardado.
     */
    @PostMapping("/guardar")
    public String guardarSlide(@RequestParam("imagen") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El archivo está vacío");
                return "redirect:/slides/listar";
            }

            if (!Files.exists(slidePath)) {
                Files.createDirectories(slidePath);
            }

            int proximoNumero = obtenerProximoNumeroSlide();
            String extension = obtenerExtension(file.getOriginalFilename());

            String nuevoNombre = "slide" + proximoNumero + extension;

            Path destino = slidePath.resolve(nuevoNombre);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            redirectAttributes.addFlashAttribute("success", "Imagen subida correctamente como: " + nuevoNombre);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }
        return "redirect:/slides/listar";
    }

    /**
     * Elimina una imagen de slide existente.
     *
     * @param nombre El nombre del archivo de la imagen a eliminar.
     * @param redirectAttributes Atributos para pasar mensajes a través de la redirección.
     * @return Una cadena de redirección a la página de listado de slides.
     */
    @PostMapping("/eliminar")
    public String eliminarSlide(@RequestParam("nombre") String nombre, RedirectAttributes redirectAttributes) {
        try {
            Path archivo = slidePath.resolve(nombre);
            Files.deleteIfExists(archivo);
            redirectAttributes.addFlashAttribute("success", "Imagen eliminada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la imagen: " + e.getMessage());
        }
        return "redirect:/slides/listar";
    }
}