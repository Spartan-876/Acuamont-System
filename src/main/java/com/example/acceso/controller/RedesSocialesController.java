package com.example.acceso.controller;

import com.example.acceso.DTO.RedSocialDTO;
import com.example.acceso.model.RedSocial;
import com.example.acceso.service.RedSocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/redes")
public class RedesSocialesController {

    private final RedSocialService redSocialService;


    public RedesSocialesController(RedSocialService redSocialService) {
        this.redSocialService = redSocialService;
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<?> listarRedesSocialesApi() {
        Map<String, Object> response = new HashMap<>();
        List<RedSocial> redesSociales = redSocialService.listarRedesSociales();
        response.put("success", true);
        response.put("data", redesSociales);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarRedSocial(@PathVariable Long id, @RequestBody RedSocialDTO redSocial) {
        RedSocial redSocialActualizada = redSocialService.ActualizarRedSocial(id, redSocial);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Red social actualizada correctamente");
        response.put("data", redSocialActualizada);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cambiarestado/{id}")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoRedSocial(@PathVariable Long id) {
        Optional<RedSocial> redSocialActualizada = redSocialService.cambiarEstadoRedSocial(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Red social actualizada correctamente");
        response.put("data", redSocialActualizada);
        return ResponseEntity.ok(response);
    }


}
