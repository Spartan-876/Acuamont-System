package com.example.acceso.service.Implements;

import com.example.acceso.DTO.RedSocialDTO;
import com.example.acceso.model.RedSocial;
import com.example.acceso.repository.RedSocialRepository;
import com.example.acceso.service.Interfaces.RedSocialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de las redes sociales.
 *
 * Proporciona métodos para listar, actualizar y cambiar el estado de las
 * redes sociales que se muestran en el sitio web.
 */
@Service
public class RedSocialServiceImpl implements RedSocialService {

    private final RedSocialRepository redSocialRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de redes
     * sociales.
     *
     * @param redSocialRepository El repositorio para las operaciones de base de
     *                            datos de {@link RedSocial}.
     */
    public RedSocialServiceImpl(RedSocialRepository redSocialRepository) {
        this.redSocialRepository = redSocialRepository;
    }

    /**
     * Obtiene una lista de todas las redes sociales, sin importar su estado.
     *
     * @return Una lista con todos los objetos {@link RedSocial}.
     */
    @Transactional(readOnly = true)
    public List<RedSocial> listarRedesSociales() {
        return redSocialRepository.findAll();
    }

    /**
     * Obtiene una lista de todas las redes sociales que están activas (estado = 1).
     *
     * @return Una lista de objetos {@link RedSocial} activos.
     */
    @Transactional(readOnly = true)
    public List<RedSocial> listarRedesSocialesActivas() {
        return redSocialRepository.findAllByEstado(1);
    }

    /**
     * Actualiza la URL de una red social existente.
     *
     * @param id           El ID de la red social a actualizar.
     * @param redSocialDTO El DTO que contiene la nueva URL.
     * @return La entidad {@link RedSocial} actualizada y guardada.
     * @throws RuntimeException si no se encuentra una red social con el ID
     *                          proporcionado.
     */
    @Transactional
    public RedSocial actualizarRedSocial(Long id, RedSocialDTO redSocialDTO) {
        RedSocial redSocialActual = redSocialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: La red social con id: " + id + " , no existe."));
        redSocialActual.setUrl(redSocialDTO.getUrl());
        return redSocialRepository.save(redSocialActual);
    }

    /**
     * Cambia el estado de una red social entre activo (1) e inactivo (0).
     *
     * @param id El ID de la red social cuyo estado se va a cambiar.
     * @return Un {@link Optional} con la red social actualizada si se encontró, o
     *         un Optional vacío si no.
     */
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
