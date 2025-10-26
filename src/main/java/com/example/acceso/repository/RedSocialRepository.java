package com.example.acceso.repository;

import com.example.acceso.model.RedSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedSocialRepository  extends JpaRepository<RedSocial, Long> {

    List<RedSocial> findAllByEstadoNot(Integer estado);

}
