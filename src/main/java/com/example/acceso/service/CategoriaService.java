package com.example.acceso.service;

import com.example.acceso.model.Categoria;
import com.example.acceso.repository.CategoriaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAllByEstadoNot(2);
    }

    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        try{
            if (categoria.getNombre()==null || categoria.getNombre().trim().isEmpty()){
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            categoria.setNombre(categoria.getNombre().trim());

            return categoriaRepository.save(categoria);
        }catch (DataIntegrityViolationException e){
            String message = e.getMessage().toLowerCase();
            if (message.contains("nombre")){
                throw new IllegalArgumentException("Ya existe una categoría con el mismo nombre");
            }else {
                throw new IllegalArgumentException("Error de integridad de datos");
            }

        }catch (Exception e){
            throw new IllegalArgumentException("Error al guardar la categoría: "+ e.getMessage(),e);

        }
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerCategoriaPorId(Long id) {
        if (id == null || id <= 0) {
           return Optional.empty();
        }

        return categoriaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.findAllByEstadoNot(2);
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerCategoriaPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre.trim().toLowerCase());
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de categoría inválido");
        }

        Categoria categoria = obtenerCategoriaPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        categoria.setEstado(2);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public Optional<Categoria> cambiarEstadoCategoria(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return obtenerCategoriaPorId(id).map(categoria -> {
            // Solo alterna entre 0 (inactivo) y 1 (activo)
            if (categoria.getEstado() == 1) {
                categoria.setEstado(0); // Desactivar
            } else if (categoria.getEstado() == 0) {
                categoria.setEstado(1); // Activar
            }
            // No se hace nada si el estado es 2 (eliminado)
            return categoriaRepository.save(categoria);
        });
    }

    @Transactional(readOnly = true)
    public boolean existeCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        // Utiliza el método eficiente del repositorio
        return categoriaRepository.existsByNombre(nombre.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public long contarCategorias() {
        return categoriaRepository.countByEstadoNot(2);
    }


}
