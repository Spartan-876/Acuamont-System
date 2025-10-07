package com.example.acceso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/logo")
public class LogoController {

    private final Path logoPath = Paths.get("iconos/");
    private final String logoFileName = "logo2.jpg";

    @GetMapping
    public String mostrarLogoPage() {
        return "redirect:/slides/listar";
    }

    @PostMapping("/guardar")
    public String guardarLogo(@RequestParam("logo") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El archivo está vacío");
                return "redirect:/logo";
            }

            if (!Files.exists(logoPath)) {
                Files.createDirectories(logoPath);
            }

            // Mantener extensión según el archivo subido
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            Path destino = logoPath.resolve("logo2" + extension);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            redirectAttributes.addFlashAttribute("success", "Logo actualizado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el logo: " + e.getMessage());
        }
        return "redirect:/logo";
    }
}

