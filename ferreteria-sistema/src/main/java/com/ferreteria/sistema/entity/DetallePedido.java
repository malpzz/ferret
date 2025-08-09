package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa el detalle de productos en cada pedido
 * 
 * Esta entidad almacena información específica de cada producto
 * solicitado en un pedido a proveedores.
 */
@Entity
@Table(name = "DETALLEPEDIDO", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"IDPEDIDO", "IDPRODUCTO"}))
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDDETALLE")
    private Long idDetalle;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor que cero")
    @Column(name = "PRECIOUNI", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación muchos a uno con pedido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPEDIDO", nullable = false)
    private Pedido pedido;

    // Relación muchos a uno con producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTO", nullable = false)
    private Producto producto;

    // Constructor por defecto
    public DetallePedido() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public DetallePedido(BigDecimal precioUnitario, Integer cantidad, Producto producto) {
        this();
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.producto = producto;
    }

    // Getters y Setters
    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    // Métodos de utilidad
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal getValorTotal() {
        return getSubtotal();
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedido)) return false;
        DetallePedido that = (DetallePedido) o;
        return idDetalle != null && idDetalle.equals(that.idDetalle);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DetallePedido{" +
                "idDetalle=" + idDetalle +
                ", precioUnitario=" + precioUnitario +
                ", cantidad=" + cantidad +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}

