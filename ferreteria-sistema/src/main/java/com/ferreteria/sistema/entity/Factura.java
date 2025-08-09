package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa las facturas de venta a clientes
 * 
 * Esta entidad gestiona todas las ventas realizadas en la ferretería,
 * incluyendo los detalles de productos vendidos y totales.
 */
@Entity
@Table(name = "FACTURA")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDFACTURA")
    private Long idFactura;

    @NotBlank(message = "El número de factura es obligatorio")
    @Size(max = 20, message = "El número de factura no puede exceder 20 caracteres")
    @Column(name = "NUMERO_FACTURA", nullable = false, unique = true, length = 20)
    private String numeroFactura;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total debe ser positivo o cero")
    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @PositiveOrZero(message = "El subtotal debe ser positivo o cero")
    @Column(name = "SUBTOTAL", precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @PositiveOrZero(message = "El impuesto debe ser positivo o cero")
    @Column(name = "IMPUESTO", precision = 12, scale = 2)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @PositiveOrZero(message = "El descuento debe ser positivo o cero")
    @Column(name = "DESCUENTO", precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "METODO_PAGO", length = 30)
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    @Column(name = "OBSERVACIONES", length = 300)
    private String observaciones;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación muchos a uno con cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCLIENTE", nullable = false)
    private Cliente cliente;

    // Relación muchos a uno con usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUSUARIO")
    private Usuario usuario;

    // Relación uno a muchos con detalles de factura
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DetalleFactura> detalles = new HashSet<>();

    // Enumeraciones
    public enum EstadoFactura {
        PENDIENTE, PAGADA, ANULADA
    }

    public enum MetodoPago {
        EFECTIVO, TARJETA, TRANSFERENCIA, CREDITO
    }

    // Constructor por defecto
    public Factura() {
        this.fecha = LocalDate.now();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Factura(String numeroFactura, Cliente cliente, Usuario usuario) {
        this();
        this.numeroFactura = numeroFactura;
        this.cliente = cliente;
        this.usuario = usuario;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Long idFactura) {
        this.idFactura = idFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<DetalleFactura> getDetalles() {
        return detalles;
    }

    public void setDetalles(Set<DetalleFactura> detalles) {
        this.detalles = detalles;
    }

    // Métodos de utilidad
    public void addDetalle(DetalleFactura detalle) {
        detalles.add(detalle);
        detalle.setFactura(this);
        recalcularTotales();
    }

    public void removeDetalle(DetalleFactura detalle) {
        detalles.remove(detalle);
        detalle.setFactura(null);
        recalcularTotales();
    }

    public void recalcularTotales() {
        this.subtotal = detalles.stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.subtotal = this.subtotal.subtract(this.descuento);
        this.impuesto = this.subtotal.multiply(BigDecimal.valueOf(0.15)); // 15% IVA
        this.total = this.subtotal.add(this.impuesto);
    }

    public int getCantidadItems() {
        return detalles.size();
    }

    public int getCantidadProductos() {
        return detalles.stream()
                .mapToInt(DetalleFactura::getCantidad)
                .sum();
    }

    public boolean puedeSerAnulada() {
        return estado == EstadoFactura.PENDIENTE;
    }

    public boolean estaPagada() {
        return estado == EstadoFactura.PAGADA;
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura factura = (Factura) o;
        return idFactura != null && idFactura.equals(factura.idFactura);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado=" + estado +
                ", metodoPago=" + metodoPago +
                '}';
    }
}

