package com.example.acceso.repository;

import com.example.acceso.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Categoria> findAllByEstadoNot(Integer estado);

    Long countByEstadoNot(Integer estado);

    List<Categoria> findByEstadoTrue();

}
