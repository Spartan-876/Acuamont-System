package com.example.acceso.controller;

import com.example.acceso.model.RedSocial;
import com.example.acceso.service.RedSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerRedes {

    private final RedSocialService redSocialService;

    @Autowired
    public GlobalControllerRedes(RedSocialService redSocialService) {
        this.redSocialService = redSocialService;
    }

    @ModelAttribute("redesSocialesActivas")
    public List<RedSocial> cargarRedesSocialesGlobales() {

        return redSocialService.listarRedesSociales()
                .stream()
                .filter(red -> red.getEstado() == 1)
                .toList();
    }

}
