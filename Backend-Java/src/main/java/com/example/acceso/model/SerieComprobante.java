package com.example.acceso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "series_comprobante")
public class SerieComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La serie es obligatorio")
    @Size(min = 2, max = 100, message = "La serie debe tener entre 2 y 20 caracteres")
    @Column(nullable = false, length = 100)
    private String serie;

    @NotNull(message = "El correlativo actual no puede ser nulo")
    @Column(nullable = false)
    private Integer correlativo_actual;

    @Column(nullable = false)
    private Integer estado = 1;

    public SerieComprobante() {
    }

    public SerieComprobante(String nombre, String serie, Integer correlativo_actual) {
        this.nombre = nombre;
        this.serie = serie;
        this.correlativo_actual = correlativo_actual;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public Integer getCorrelativo_actual() {
        return correlativo_actual;
    }

    public void setCorrelativo_actual(Integer correlativo_actual) {
        this.correlativo_actual = correlativo_actual;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}
