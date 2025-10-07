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

@Controller
@RequestMapping("/slides")
public class SlidesController {

    private final Path slidePath = Paths.get("slide-Inicio/");

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

    private String obtenerExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    @GetMapping("/listar")
    public String listarSlides(Model model) throws IOException {
        model.addAttribute("slides", obtenerSlides());
        return "slides";
    }

    @GetMapping("/PrincipalPage-web")
    public String paginaPrincipal(Model model) throws IOException {
        model.addAttribute("slides", obtenerSlides());
        return "principal-page";
    }

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