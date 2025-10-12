package com.example.acceso.repository;

import com.example.acceso.model.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    List<Cuota> findAllByEstadoNot(Integer estado);

    Long countByEstado(Integer estado);

}
