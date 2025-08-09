package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los proveedores de la ferretería
 * 
 * Esta entidad almacena la información de las empresas que
 * suministran productos a la ferretería.
 */
@Entity
@Table(name = "PROVEEDORES")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPROVEEDOR")
    private Long idProveedor;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "NOMBREPROVEEDOR", nullable = false, length = 100)
    private String nombreProveedor;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
    @Column(name = "DIRECCION", nullable = false, length = 150)
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9-]+$", message = "El teléfono debe contener solo números y guiones")
    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    @Column(name = "TELEFONO", nullable = false, length = 15)
    private String telefono;

    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @Size(max = 100, message = "El nombre del contacto no puede exceder 100 caracteres")
    @Column(name = "CONTACTO_PRINCIPAL", length = 100)
    private String contactoPrincipal;

    @Size(max = 20, message = "El RUC no puede exceder 20 caracteres")
    @Column(name = "RUC", unique = true, length = 20)
    private String ruc;

    @Size(max = 100, message = "Las condiciones de pago no pueden exceder 100 caracteres")
    @Column(name = "CONDICIONES_PAGO", length = 100)
    private String condicionesPago = "CONTADO";

    @DecimalMin(value = "1.0", message = "La calificación debe ser mínimo 1.0")
    @DecimalMax(value = "5.0", message = "La calificación debe ser máximo 5.0")
    @Column(name = "CALIFICACION", precision = 2, scale = 1)
    private BigDecimal calificacion = BigDecimal.valueOf(5.0);

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "FECHA_REGISTRO", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación uno a muchos con productos
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Producto> productos = new HashSet<>();

    // Relación uno a muchos con pedidos
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Pedido> pedidos = new HashSet<>();

    // Constructor por defecto
    public Proveedor() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Proveedor(String nombreProveedor, String direccion, String telefono, String email) {
        this();
        this.nombreProveedor = nombreProveedor;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Long idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactoPrincipal() {
        return contactoPrincipal;
    }

    public void setContactoPrincipal(String contactoPrincipal) {
        this.contactoPrincipal = contactoPrincipal;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getCondicionesPago() {
        return condicionesPago;
    }

    public void setCondicionesPago(String condicionesPago) {
        this.condicionesPago = condicionesPago;
    }

    public BigDecimal getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(BigDecimal calificacion) {
        this.calificacion = calificacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Set<Producto> getProductos() {
        return productos;
    }

    public void setProductos(Set<Producto> productos) {
        this.productos = productos;
    }

    public Set<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(Set<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    // Métodos de utilidad
    public void addProducto(Producto producto) {
        productos.add(producto);
        producto.setProveedor(this);
    }

    public void removeProducto(Producto producto) {
        productos.remove(producto);
        producto.setProveedor(null);
    }

    public void addPedido(Pedido pedido) {
        pedidos.add(pedido);
        pedido.setProveedor(this);
    }

    public void removePedido(Pedido pedido) {
        pedidos.remove(pedido);
        pedido.setProveedor(null);
    }

    public long getCantidadProductos() {
        return productos.stream()
                .filter(p -> p.getActivo())
                .count();
    }

    public boolean esCalificacionBuena() {
        return calificacion.compareTo(BigDecimal.valueOf(4.0)) >= 0;
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proveedor)) return false;
        Proveedor proveedor = (Proveedor) o;
        return idProveedor != null && idProveedor.equals(proveedor.idProveedor);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Proveedor{" +
                "idProveedor=" + idProveedor +
                ", nombreProveedor='" + nombreProveedor + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", calificacion=" + calificacion +
                ", activo=" + activo +
                '}';
    }
}

