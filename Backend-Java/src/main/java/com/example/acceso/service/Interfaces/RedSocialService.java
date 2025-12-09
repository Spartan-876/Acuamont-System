package com.example.acceso.service.Interfaces;

import com.example.acceso.DTO.RedSocialDTO;
import com.example.acceso.model.RedSocial;

import java.util.List;
import java.util.Optional;

public interface RedSocialService {

    List<RedSocial> listarRedesSociales();

    List<RedSocial> listarRedesSocialesActivas();

    RedSocial actualizarRedSocial(Long id, RedSocialDTO redSocialDTO);

    Optional<RedSocial> cambiarEstadoRedSocial(Long id);

}
