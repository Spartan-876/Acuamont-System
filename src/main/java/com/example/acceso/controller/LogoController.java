package com.example.acceso.controller;

import org.springframework.stereotype.Controller;
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

/**
 * Controlador para gestionar la carga y actualización del logo de la
 * aplicación.
 *
 * Proporciona endpoints para subir un nuevo archivo de logo que será utilizado
 * en el sitio.
 */
@Controller
@RequestMapping("/logo")
public class LogoController {

    /**
     * Ruta del directorio donde se almacenan los íconos y logos de la aplicación.
     */
    private final Path logoPath = Paths.get("iconos/");

    /**
     * Muestra la página de gestión de logos.
     *
     * Actualmente, este método redirige a la página de gestión de slides,
     * sugiriendo que la funcionalidad de cambio de logo puede estar integrada
     * en esa interfaz.
     *
     * @return Una cadena de redirección a "/slides/listar".
     */
    @GetMapping
    public String mostrarLogoPage() {
        return "redirect:/slides/listar";
    }

    /**
     * Guarda o actualiza el archivo del logo de la aplicación.
     *
     * @param file               El archivo de imagen del logo subido a través del
     *                           formulario.
     * @param redirectAttributes Atributos para pasar mensajes (éxito/error) a
     *                           través de la redirección.
     * @return Una cadena de redirección a la página de gestión de logos.
     */
    @PostMapping("/guardar")
    public String guardarLogo(@RequestParam("logo") MultipartFile file, RedirectAttributes redirectAttributes)
            throws IOException {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El archivo está vacío");
                return "redirect:/logo";
            }

            if (!Files.exists(logoPath)) {
                Files.createDirectories(logoPath);
            }

            String extension = obtenerExtension(file.getOriginalFilename());
            if (extension.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "El archivo no tiene una extensión válida (ej: .png, .jpg).");
                return "redirect:/logo";
            }

            Path destino = logoPath.resolve("logo2" + extension);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            redirectAttributes.addFlashAttribute("success", "Logo actualizado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el logo: " + e.getMessage());
        }
        return "redirect:/logo";
    }

    /**
     * Extrae de forma segura la extensión de un nombre de archivo.
     *
     * @param fileName El nombre completo del archivo.
     * @return La extensión con el punto (ej. ".png") o una cadena vacía si no tiene
     *         extensión o el nombre es nulo.
     */
    private String obtenerExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return fileName.substring(lastDotIndex);
    }
}
