package com.ferreteria.sistema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
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
        if (producto == null) return false;
        try {
            return this.cantidad <= producto.getStockMinimo();
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getDiferenciaMinimo() {
        if (producto == null) return null;
        try {
            return producto.getStockMinimo() - this.cantidad;
        } catch (Exception e) {
            return null;
        }
    }

    // Métodos para serialización JSON sin lazy loading
    public Long getIdProducto() {
        if (producto == null) return null;
        try {
            return producto.getIdProducto();
        } catch (Exception e) {
            return null;
        }
    }

    public String getNombreProducto() {
        if (producto == null) return null;
        try {
            return producto.getNombreProducto();
        } catch (Exception e) {
            return null;
        }
    }

    public String getCodigoProducto() {
        if (producto == null) return null;
        try {
            return producto.getCodigoProducto();
        } catch (Exception e) {
            return null;
        }
    }

    public String getCategoriaProducto() {
        if (producto == null) return null;
        try {
            return producto.getCategoria();
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getStockMinimo() {
        if (producto == null) return null;
        try {
            return producto.getStockMinimo();
        } catch (Exception e) {
            return null;
        }
    }

    public ProductoInfo getProductoInfo() {
        if (producto == null) return null;
        try {
            return new ProductoInfo(
                producto.getIdProducto(),
                producto.getNombreProducto(),
                producto.getCodigoProducto(),
                producto.getCategoria(),
                producto.getMarca(),
                producto.getUnidadMedida(),
                producto.getStockMinimo()
            );
        } catch (Exception e) {
            // En caso de lazy loading exception, devolver null
            System.out.println("DEBUG - Error accediendo a producto lazy: " + e.getMessage());
            return null;
        }
    }

    // Clase interna para serializar información básica del producto
    public static class ProductoInfo {
        private Long idProducto;
        private String nombreProducto;
        private String codigoProducto;
        private String categoria;
        private String marca;
        private String unidadMedida;
        private Integer stockMinimo;

        public ProductoInfo(Long idProducto, String nombreProducto, String codigoProducto,
                           String categoria, String marca, String unidadMedida, Integer stockMinimo) {
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.codigoProducto = codigoProducto;
            this.categoria = categoria;
            this.marca = marca;
            this.unidadMedida = unidadMedida;
            this.stockMinimo = stockMinimo;
        }

        // Getters
        public Long getIdProducto() { return idProducto; }
        public String getNombreProducto() { return nombreProducto; }
        public String getCodigoProducto() { return codigoProducto; }
        public String getCategoria() { return categoria; }
        public String getMarca() { return marca; }
        public String getUnidadMedida() { return unidadMedida; }
        public Integer getStockMinimo() { return stockMinimo; }
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

