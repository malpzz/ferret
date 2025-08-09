package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entidad que representa el control de inventario de productos
 * 
 * Esta entidad mantiene el control de las existencias de cada
 * producto en el almacén de la ferretería.
 */
@Entity
@Table(name = "STOCK")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDSTOCK")
    private Long idStock;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Size(max = 50, message = "La ubicación no puede exceder 50 caracteres")
    @Column(name = "UBICACION", length = 50)
    private String ubicacion = "ALMACEN PRINCIPAL";

    @Column(name = "FECHA_ULTIMO_MOVIMIENTO")
    private LocalDateTime fechaUltimoMovimiento;

    // Relación uno a uno con producto
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTO", nullable = false, unique = true)
    private Producto producto;

    // Constructor por defecto
    public Stock() {
        this.fechaUltimoMovimiento = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Stock(Integer cantidad, Producto producto) {
        this();
        this.cantidad = cantidad;
        this.producto = producto;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    @PrePersist
    public void actualizarFechaMovimiento() {
        this.fechaUltimoMovimiento = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdStock() {
        return idStock;
    }

    public void setIdStock(Long idStock) {
        this.idStock = idStock;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        this.fechaUltimoMovimiento = LocalDateTime.now();
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaUltimoMovimiento() {
        return fechaUltimoMovimiento;
    }

    public void setFechaUltimoMovimiento(LocalDateTime fechaUltimoMovimiento) {
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    // Métodos de utilidad para manejo de inventario
    public void incrementarStock(Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a incrementar debe ser positiva");
        }
        this.cantidad += cantidad;
        this.fechaUltimoMovimiento = LocalDateTime.now();
    }

    public void decrementarStock(Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a decrementar debe ser positiva");
        }
        if (this.cantidad < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + this.cantidad + ", Requerido: " + cantidad);
        }
        this.cantidad -= cantidad;
        this.fechaUltimoMovimiento = LocalDateTime.now();
    }

    public boolean tieneSuficienteStock(Integer cantidadRequerida) {
        return this.cantidad >= cantidadRequerida;
    }

    public boolean esBajoMinimo() {
        return producto != null && this.cantidad <= producto.getStockMinimo();
    }

    public Integer getDiferenciaMinimo() {
        if (producto == null) return 0;
        return producto.getStockMinimo() - this.cantidad;
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock)) return false;
        Stock stock = (Stock) o;
        return idStock != null && idStock.equals(stock.idStock);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Stock{" +
                "idStock=" + idStock +
                ", cantidad=" + cantidad +
                ", ubicacion='" + ubicacion + '\'' +
                ", fechaUltimoMovimiento=" + fechaUltimoMovimiento +
                '}';
    }
}

