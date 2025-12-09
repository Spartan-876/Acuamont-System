package com.example.acceso.service.Implements;

import com.example.acceso.model.Perfil;
import com.example.acceso.model.Opcion;
import com.example.acceso.repository.PerfilRepository;
import com.example.acceso.repository.OpcionRepository;
import com.example.acceso.repository.UsuarioRepository;
import com.example.acceso.service.Interfaces.PerfilService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final OpcionRepository opcionRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilServiceImpl(PerfilRepository perfilRepository, OpcionRepository opcionRepository, UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.opcionRepository = opcionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Perfil> listarPerfilesActivos() {
        return perfilRepository.findByEstado(1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Perfil> listarTodosLosPerfiles() {
        return perfilRepository.findAllByEstadoNot(2);
    }

    @Override
    @Transactional
    public Perfil guardarPerfil(Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Perfil> obtenerPerfilPorId(Long id) {
        return perfilRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<Perfil> cambiarEstadoPerfil(Long id) {
        return perfilRepository.findById(id).map(perfil -> {
            if (perfil.getEstado() == 1) {
                perfil.setEstado(0);
            } else if (perfil.getEstado() == 0) {
                perfil.setEstado(1);
            }
            return perfilRepository.save(perfil);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Opcion> listarTodasLasOpciones() {
        return opcionRepository.findAll();
    }

    @Override
    @Transactional
    public void eliminarPerfil(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El perfil con ID " + id + " no existe."));

        long usuariosActivos = usuarioRepository.countByPerfilAndEstado(perfil, 1);
        if (usuariosActivos > 0) {
            throw new IllegalStateException("No se puede eliminar el perfil porque está asignado a " + usuariosActivos + " usuario(s) activo(s).");
        }

        perfil.setEstado(2);
        perfilRepository.save(perfil);
    }
}
