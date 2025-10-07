package com.example.acceso.repository;

import com.example.acceso.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository  extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Producto> findAllByEstadoNot(Integer estado);

    Long countByEstadoNot(Integer estado);

}
