package com.example.acceso.service.Implements;

import com.example.acceso.model.Perfil;
import com.example.acceso.model.Opcion;
import com.example.acceso.repository.PerfilRepository;
import com.example.acceso.repository.OpcionRepository;
import com.example.acceso.service.Interfaces.PerfilService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para gestionar la lógica de negocio de los perfiles de usuario.
 *
 * Esta clase implementa la interfaz {@link PerfilService} y se encarga de las
 * operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre las entidades
 * de Perfil y Opcion, interactuando con los repositorios correspondientes.
 */
@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final OpcionRepository opcionRepository;

    /**
     * Constructor para la inyección de dependencias de los repositorios necesarios.
     *
     * @param perfilRepository Repositorio para las operaciones de base de datos de {@link Perfil}.
     * @param opcionRepository Repositorio para las operaciones de base de datos de {@link Opcion}.
     */
    public PerfilServiceImpl(PerfilRepository perfilRepository, OpcionRepository opcionRepository) {
        this.perfilRepository = perfilRepository;
        this.opcionRepository = opcionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Perfil> listarPerfilesActivos() {
        return perfilRepository.findByEstadoTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Perfil> listarTodosLosPerfiles() {
        return perfilRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Perfil guardarPerfil(Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Perfil> obtenerPerfilPorId(Long id) {
        return perfilRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Optional<Perfil> cambiarEstadoPerfil(Long id) {
        return perfilRepository.findById(id).map(perfil -> {
            perfil.setEstado(!perfil.isEstado());
            return perfilRepository.save(perfil);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Opcion> listarTodasLasOpciones() {
        return opcionRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Advertencia:</strong> Este método realiza un borrado físico del perfil.
     * Si el perfil está asignado a usuarios, podría causar errores de integridad referencial.
     */
    @Override
    @Transactional
    public void eliminarPerfil(Long id) {
        perfilRepository.deleteById(id);
    }
}