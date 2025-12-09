package com.example.acceso.service.Interfaces;

import com.example.acceso.DTO.AjusteInventarioDTO;
import com.example.acceso.model.AjusteInventario;

import java.util.List;

public interface AjusteInventarioService {

    AjusteInventario guardarAjuste(AjusteInventarioDTO ajusteInventarioDTO);

    List<AjusteInventario> listarAjustePorProducto(Long id);
}
