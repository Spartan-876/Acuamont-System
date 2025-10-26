package com.example.acceso.service;

import com.example.acceso.DTO.RedSocialDTO;
import com.example.acceso.model.RedSocial;
import com.example.acceso.repository.RedSocialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RedSocialService {

    private final RedSocialRepository redSocialRepository;


    public RedSocialService(RedSocialRepository redSocialRepository) {
        this.redSocialRepository = redSocialRepository;
    }

    @Transactional(readOnly = true)
    public List<RedSocial> listarRedesSociales() {
        return redSocialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RedSocial> listarRedesSocialesActivas() {
        return redSocialRepository.findAllByEstadoNot(0);
    }

    @Transactional
    public RedSocial ActualizarRedSocial(Long id,RedSocialDTO redSocial) {
        RedSocial redSocialActual = redSocialRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Error: La red social con id: "+ id + " , no existe."));
        redSocialActual.setUrl(redSocial.getUrl());
        return redSocialRepository.save(redSocialActual);
    }

    @Transactional
    public Optional<RedSocial> cambiarEstadoRedSocial(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return redSocialRepository.findById(id).map(redSocial -> {
            if (redSocial.getEstado() == 1) {
                redSocial.setEstado(0);
            } else if (redSocial.getEstado() == 0) {
                redSocial.setEstado(1);
            }
            return redSocialRepository.save(redSocial);
        });
    }

}
