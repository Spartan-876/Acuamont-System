package com.example.acceso.service.Interfaces;

import com.example.acceso.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {

    List<Categoria> listarCategorias();

    Categoria guardarCategoria(Categoria categoria);

    Optional<Categoria> obtenerCategoriaPorId(Long id);

    List<Categoria> listarCategoriasActivas();

    Optional<Categoria> obtenerCategoriaPorNombre(String nombre);

    void eliminarCategoria(Long id);

    Optional<Categoria> cambiarEstadoCategoria(Long id);

    boolean existeCategoria(String nombre);

    long contarCategorias();

}
