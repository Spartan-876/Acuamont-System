package com.example.acceso.repository;

import com.example.acceso.model.SerieComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerieComprobanteRepository extends JpaRepository<SerieComprobante, Long> {

    Optional<SerieComprobante> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<SerieComprobante> findAllByEstadoNot(Integer estado);

    List<SerieComprobante> findAllByEstadoTrue();

}
