package com.example.acceso.controller;

import com.cloudinary.utils.ObjectUtils;
import com.example.acceso.service.Implements.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/slides")
public class SlidesController {

    private final CloudinaryService cloudinaryService;

    // Nombre de la carpeta en la nube
    private final String CARPETA_SLIDES = "slides_inicio";

    public SlidesController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }


    @GetMapping("/listar")
    public String listarSlides(Model model) {
        List<String> slides = cloudinaryService.listarImagenesDeCarpeta(CARPETA_SLIDES);
        model.addAttribute("slides", slides);
        return "slides";
    }

    @GetMapping("/api/listar-urls")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerSlidesJson() {
        try {
            List<String> slides = cloudinaryService.listarImagenesDeCarpeta(CARPETA_SLIDES);
            return ResponseEntity.ok(slides);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }


    @GetMapping("/PrincipalPage-web")
    public String paginaPrincipal(Model model) {
        // 1. Llamamos al servicio
        List<String> slides = cloudinaryService.listarImagenesDeCarpeta(CARPETA_SLIDES);

        // 2. IMPRIMIR EN CONSOLA (DEBUG)
        System.out.println("--- DEBUG SLIDES ---");
        System.out.println("Carpeta buscada: " + CARPETA_SLIDES);
        System.out.println("Cantidad encontrada: " + (slides != null ? slides.size() : "NULL"));
        if (slides != null && !slides.isEmpty()) {
            System.out.println("Primera URL: " + slides.get(0));
        }
        System.out.println("--------------------");

        model.addAttribute("slides", slides);
        return "principal-page";
    }

    @PostMapping("/guardar")
    public String guardarSlide(@RequestParam("imagen") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El archivo está vacío");
                return "redirect:/slides/listar";
            }

            cloudinaryService.subirImagen(file, CARPETA_SLIDES);

            redirectAttributes.addFlashAttribute("success", "Slide subido correctamente a la nube");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir: " + e.getMessage());
        }
        return "redirect:/slides/listar";
    }

    @PostMapping("/eliminar")
    public String eliminarSlide(@RequestParam("nombre") String urlImagen, RedirectAttributes redirectAttributes) {
        try {
            // Extraemos el ID público de la URL para poder borrarlo
            String publicId = obtenerPublicId(urlImagen);

            if (publicId != null) {
                cloudinaryService.eliminarImagen(publicId, ObjectUtils.emptyMap());
                redirectAttributes.addFlashAttribute("success", "Imagen eliminada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo identificar la imagen para borrar");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/slides/listar";
    }

    private String obtenerPublicId(String url) {
        try {
            int inicio = url.indexOf(CARPETA_SLIDES + "/");
            if (inicio == -1) return null;

            String rutaConExt = url.substring(inicio);

            int punto = rutaConExt.lastIndexOf(".");
            if (punto != -1) {
                return rutaConExt.substring(0, punto);
            }
            return rutaConExt;
        } catch (Exception e) {
            return null;
        }
    }
}