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
 * Entidad que representa las órdenes de compra a proveedores
 * 
 * Esta entidad gestiona todos los pedidos realizados a proveedores
 * para el reabastecimiento del inventario de la ferretería.
 */
@Entity
@Table(name = "PEDIDOS")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPEDIDO")
    private Long idPedido;

    @NotBlank(message = "El número de pedido es obligatorio")
    @Size(max = 20, message = "El número de pedido no puede exceder 20 caracteres")
    @Column(name = "NUMERO_PEDIDO", nullable = false, unique = true, length = 20)
    private String numeroPedido;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total debe ser positivo o cero")
    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;

    @Column(name = "FECHA_ENTREGA_ESPERADA")
    private LocalDate fechaEntregaEsperada;

    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    @Column(name = "OBSERVACIONES", length = 300)
    private String observaciones;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación muchos a uno con proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPROVEEDOR", nullable = false)
    private Proveedor proveedor;

    // Relación muchos a uno con usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUSUARIO")
    private Usuario usuario;

    // Relación uno a muchos con detalles de pedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DetallePedido> detalles = new HashSet<>();

    // Enumeración para estados de pedido
    public enum EstadoPedido {
        PENDIENTE, APROBADO, ENVIADO, RECIBIDO, CANCELADO
    }

    // Constructor por defecto
    public Pedido() {
        this.fecha = LocalDate.now();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Pedido(String numeroPedido, Proveedor proveedor, Usuario usuario) {
        this();
        this.numeroPedido = numeroPedido;
        this.proveedor = proveedor;
        this.usuario = usuario;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
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

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaEntregaEsperada() {
        return fechaEntregaEsperada;
    }

    public void setFechaEntregaEsperada(LocalDate fechaEntregaEsperada) {
        this.fechaEntregaEsperada = fechaEntregaEsperada;
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

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(Set<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    // Métodos de utilidad
    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
        recalcularTotal();
    }

    public void removeDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        detalle.setPedido(null);
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCantidadItems() {
        return detalles.size();
    }

    public int getCantidadProductos() {
        return detalles.stream()
                .mapToInt(DetallePedido::getCantidad)
                .sum();
    }

    public boolean puedeSerCancelado() {
        return estado == EstadoPedido.PENDIENTE || estado == EstadoPedido.APROBADO;
    }

    public boolean puedeSerAprobado() {
        return estado == EstadoPedido.PENDIENTE;
    }

    public boolean puedeSerRecibido() {
        return estado == EstadoPedido.ENVIADO;
    }

    public boolean estaVencido() {
        return fechaEntregaEsperada != null && fechaEntregaEsperada.isBefore(LocalDate.now());
    }

    public long getDiasVencimiento() {
        if (fechaEntregaEsperada == null) return 0;
        return LocalDate.now().toEpochDay() - fechaEntregaEsperada.toEpochDay();
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido)) return false;
        Pedido pedido = (Pedido) o;
        return idPedido != null && idPedido.equals(pedido.idPedido);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", numeroPedido='" + numeroPedido + '\'' +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado=" + estado +
                ", fechaEntregaEsperada=" + fechaEntregaEsperada +
                '}';
    }
}

