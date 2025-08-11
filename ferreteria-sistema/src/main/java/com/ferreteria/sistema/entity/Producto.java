package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los productos del catálogo de la ferretería
 * 
 * Esta entidad almacena información completa de cada producto
 * disponible en el inventario de la ferretería.
 */
@Entity
@Table(name = "PRODUCTOS")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPRODUCTO")
    private Long idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "NOMBREPRODUCTO", nullable = false, length = 100)
    private String nombreProducto;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Column(name = "CODIGO_PRODUCTO", nullable = false, unique = true, length = 50)
    private String codigoProducto;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Column(name = "CATEGORIA", nullable = false, length = 50)
    private String categoria;

    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    @Column(name = "MARCA", length = 50)
    private String marca;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que cero")
    @Column(name = "PRECIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Positive(message = "El precio de compra debe ser mayor que cero")
    @Column(name = "PRECIO_COMPRA", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    @Column(name = "UNIDAD_MEDIDA", length = 20)
    private String unidadMedida = "UNIDAD";

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "STOCK_MINIMO")
    private Integer stockMinimo = 0;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación muchos a uno con proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPROVEEDOR", nullable = false)
    private Proveedor proveedor;

    // Relación uno a uno con stock
    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stock stock;

    // Relación uno a muchos con detalles de pedido
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DetallePedido> detallesPedido = new HashSet<>();

    // Relación uno a muchos con detalles de factura
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DetalleFactura> detallesFactura = new HashSet<>();

    // Constructor por defecto
    public Producto() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Producto(String nombreProducto, String codigoProducto, String categoria, BigDecimal precio) {
        this();
        this.nombreProducto = nombreProducto;
        this.codigoProducto = codigoProducto;
        this.categoria = categoria;
        this.precio = precio;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
        if (stock != null) {
            stock.setProducto(this);
        }
    }

    public Set<DetallePedido> getDetallesPedido() {
        return detallesPedido;
    }

    public void setDetallesPedido(Set<DetallePedido> detallesPedido) {
        this.detallesPedido = detallesPedido;
    }

    public Set<DetalleFactura> getDetallesFactura() {
        return detallesFactura;
    }

    public void setDetallesFactura(Set<DetalleFactura> detallesFactura) {
        this.detallesFactura = detallesFactura;
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombreProducto + (marca != null ? " - " + marca : "");
    }

    // Método para exponer idProveedor en JSON sin mapeo JPA
    @Transient
    public Long getIdProveedor() {
        return proveedor != null ? proveedor.getIdProveedor() : null;
    }

    // Método para asignar proveedor por ID (útil para deserialización JSON)
    @Transient
    public void setIdProveedor(Long idProveedor) {
        if (idProveedor != null) {
            if (this.proveedor == null) {
                this.proveedor = new Proveedor();
            }
            this.proveedor.setIdProveedor(idProveedor);
        }
    }

    public BigDecimal calcularMargenGanancia() {
        if (precioCompra == null || precioCompra.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precio.subtract(precioCompra).divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP);
    }

    public boolean requiereReabastecimiento() {
        if (stock == null) return true;
        return stock.getCantidad() <= stockMinimo;
    }

    @Transient
    public Integer getCantidadStock() {
        return stock != null ? stock.getCantidad() : 0;
    }

    public boolean tieneStockSuficiente(Integer cantidadRequerida) {
        return getCantidadStock() >= cantidadRequerida;
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto producto = (Producto) o;
        return idProducto != null && idProducto.equals(producto.idProducto);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", codigoProducto='" + codigoProducto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", marca='" + marca + '\'' +
                ", precio=" + precio +
                ", activo=" + activo +
                '}';
    }
}

