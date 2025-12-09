package com.example.acceso.repository;

import com.example.acceso.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    
    List<Perfil> findByEstado(Integer estado);

    List<Perfil> findAllByEstadoNot(Integer estado);
}
