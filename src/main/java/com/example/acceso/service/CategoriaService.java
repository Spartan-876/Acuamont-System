package com.example.acceso.service;

import com.example.acceso.model.Categoria;
import com.example.acceso.repository.CategoriaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de las categorías.
 *
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * sobre las entidades de Categoria, manejando la lógica de negocio como
 * validaciones y transacciones.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio de categorías.
     *
     * @param categoriaRepository El repositorio para las operaciones de base de
     *                            datos de Categoria.
     */
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Obtiene una lista de todas las categorías que no están eliminadas
     * lógicamente.
     *
     * @return Una lista de objetos {@link Categoria}.
     */
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAllByEstadoNot(2);
    }

    /**
     * Guarda o actualiza una categoría en la base de datos.
     * <p>
     * Si la categoría tiene un ID, se actualiza. Si no, se crea una nueva.
     * Realiza validaciones para asegurar que el nombre no esté vacío y sea único.
     *
     * @param categoria El objeto {@link Categoria} a guardar.
     * @return La categoría guardada con su ID asignado o actualizado.
     * @throws IllegalArgumentException Si el nombre está vacío, si ya existe una
     *                                  categoría con el mismo nombre,
     *                                  o si ocurre otro error de integridad de
     *                                  datos.
     */
    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        try {
            if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            categoria.setNombre(categoria.getNombre().trim());

            return categoriaRepository.save(categoria);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("nombre")) {
                throw new IllegalArgumentException("Ya existe una categoría con el mismo nombre");
            } else {
                throw new IllegalArgumentException("Error de integridad de datos");
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al guardar la categoría: " + e.getMessage(), e);

        }
    }

    /**
     * Busca una categoría por su ID.
     *
     * @param id El ID de la categoría a buscar.
     * @return Un {@link Optional} que contiene la categoría si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerCategoriaPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return categoriaRepository.findById(id);
    }

    /**
     * Obtiene una lista de todas las categorías activas (estado no es 2).
     *
     * @return Una lista de objetos {@link Categoria} activos.
     */
    @Transactional(readOnly = true)
    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.findAllByEstadoNot(2);
    }

    /**
     * Busca una categoría por su nombre.
     *
     * @param nombre El nombre de la categoría a buscar.
     * @return Un {@link Optional} que contiene la categoría si se encuentra, o un
     *         Optional vacío si no.
     */
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerCategoriaPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre.trim().toLowerCase());
    }

    /**
     * Realiza el borrado lógico de una categoría, cambiando su estado a 2.
     *
     * @param id El ID de la categoría a eliminar.
     * @throws IllegalArgumentException si el ID es inválido o la categoría no se
     *                                  encuentra.
     */
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

    /**
     * Cambia el estado de una categoría entre activo (1) e inactivo (0).
     * <p>
     * Si la categoría está eliminada (estado 2), no se realiza ningún cambio.
     *
     * @param id El ID de la categoría cuyo estado se va a cambiar.
     * @return Un {@link Optional} con la categoría actualizada si se encontró, o un
     *         Optional vacío si no.
     */
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

    /**
     * Verifica si ya existe una categoría con un nombre específico.
     *
     * @param nombre El nombre a verificar.
     * @return {@code true} si la categoría existe, {@code false} en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean existeCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        // Utiliza el método eficiente del repositorio
        return categoriaRepository.existsByNombre(nombre.trim().toLowerCase());
    }

    /**
     * Cuenta el número total de categorías que no están eliminadas.
     *
     * @return El número de categorías activas e inactivas.
     */
    @Transactional(readOnly = true)
    public long contarCategorias() {
        return categoriaRepository.countByEstadoNot(2);
    }

}
