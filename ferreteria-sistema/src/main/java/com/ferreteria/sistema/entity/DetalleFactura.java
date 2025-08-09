package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa el detalle de productos en cada factura
 * 
 * Esta entidad almacena información específica de cada producto
 * vendido en una factura, incluyendo precios y cantidades.
 */
@Entity
@Table(name = "DETALLEFACTURA", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"IDFACTURA", "IDPRODUCTO"}))
public class DetalleFactura {

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

    @PositiveOrZero(message = "El descuento del item debe ser positivo o cero")
    @Column(name = "DESCUENTO_ITEM", precision = 10, scale = 2)
    private BigDecimal descuentoItem = BigDecimal.ZERO;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación muchos a uno con factura
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFACTURA", nullable = false)
    private Factura factura;

    // Relación muchos a uno con producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTO", nullable = false)
    private Producto producto;

    // Constructor por defecto
    public DetalleFactura() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public DetalleFactura(BigDecimal precioUnitario, Integer cantidad, Producto producto) {
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

    public BigDecimal getDescuentoItem() {
        return descuentoItem;
    }

    public void setDescuentoItem(BigDecimal descuentoItem) {
        this.descuentoItem = descuentoItem;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    // Métodos de utilidad
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return subtotal.subtract(descuentoItem);
    }

    public BigDecimal getPrecioConDescuento() {
        if (descuentoItem.compareTo(BigDecimal.ZERO) == 0) {
            return precioUnitario;
        }
        BigDecimal descuentoPorUnidad = descuentoItem.divide(BigDecimal.valueOf(cantidad), 2, BigDecimal.ROUND_HALF_UP);
        return precioUnitario.subtract(descuentoPorUnidad);
    }

    public BigDecimal getPorcentajeDescuento() {
        if (precioUnitario.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalSinDescuento = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return descuentoItem.divide(totalSinDescuento, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleFactura)) return false;
        DetalleFactura that = (DetalleFactura) o;
        return idDetalle != null && idDetalle.equals(that.idDetalle);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DetalleFactura{" +
                "idDetalle=" + idDetalle +
                ", precioUnitario=" + precioUnitario +
                ", cantidad=" + cantidad +
                ", descuentoItem=" + descuentoItem +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}

