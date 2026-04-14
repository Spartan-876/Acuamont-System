package com.example.acceso.controller;

import com.example.acceso.service.Implements.CloudinaryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/logo")
public class LogoController {

    private final CloudinaryService cloudinaryService;

    public LogoController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

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
            String urlLogo = cloudinaryService.subirImagenConNombreFijo(file, "logo2");
            redirectAttributes.addFlashAttribute("success", "Logo actualizado correctamente. Puede tardar unos minutos en reflejarse por el caché.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el logo: " + e.getMessage());
        }
        return "redirect:/logo";
    }
}