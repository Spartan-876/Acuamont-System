package com.example.acceso.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 2, max = 255, message = "La descripción debe tener entre 2 y 255 caracteres")
    @Column(nullable = false, length = 255)
    private String descripcion;

    @NotNull(message = "El precio de compra es obligatorio")
    @Min(value = 0, message = "El precio de compra debe ser mayor o igual a 0")
    @Column(nullable = false)
    private Double precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Min(value = 0, message = "El precio de venta debe ser mayor o igual a 0")
    @Column(nullable = false)
    private Double precioVenta;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    @Column(nullable = false)
    private Integer stock;

    @NotNull(message = "El stock de seguridad es obligatorio")
    @Min(value = 0, message = "El stock de seguridad debe ser mayor o igual a 0")
    @Column(nullable = false)
    private Integer stockSeguridad;

    @Column(columnDefinition = "TEXT")
    private String imagen;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(nullable = false)
    private Integer estado = 1;

    public Producto() {
    }

    public Producto(String nombre, String descripcion, Double precioCompra, Double precioVenta, Integer stock, Integer stockSeguridad, String imagen, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockSeguridad = stockSeguridad;
        this.imagen = imagen;
        this.categoria = categoria;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockSeguridad() {
        return stockSeguridad;
    }

    public void setStockSeguridad(Integer stockSeguridad) {
        this.stockSeguridad = stockSeguridad;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precioCompra=" + precioCompra +
                ", precioVenta=" + precioVenta +
                ", stock=" + stock +
                ", stockSeguridad=" + stockSeguridad +
                ", imagen='" + imagen + '\'' +
                ", categoria=" + (categoria != null ? categoria.getNombre() : null) +
                ", estado=" + estado +
                '}';
    }

}
