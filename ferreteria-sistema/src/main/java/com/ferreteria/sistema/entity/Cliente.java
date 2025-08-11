package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los clientes de la ferretería
 * 
 * Esta entidad almacena la información personal y comercial
 * de los clientes que realizan compras en la ferretería.
 */
@Entity
@Table(name = "CLIENTES")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDCLIENTE")
    private Long idCliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "NOMBRECLIENTE", nullable = false, length = 100)
    private String nombreCliente;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    @Column(name = "APELLIDOS", nullable = false, length = 100)
    private String apellidos;

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

    @Size(max = 20, message = "La cédula no puede exceder 20 caracteres")
    @Column(name = "CEDULA", unique = true, length = 20)
    private String cedula;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CLIENTE", length = 20)
    private TipoCliente tipoCliente = TipoCliente.REGULAR;

    @Column(name = "LIMITE_CREDITO", precision = 10, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "FECHA_REGISTRO", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación uno a muchos con facturas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Factura> facturas = new HashSet<>();

    // Enumeración para tipos de cliente
    public enum TipoCliente {
        REGULAR, MAYORISTA, VIP
    }

    // Constructor por defecto
    public Cliente() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Cliente(String nombreCliente, String apellidos, String direccion, String telefono, String email) {
        this();
        this.nombreCliente = nombreCliente;
        this.apellidos = apellidos;
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
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
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

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public BigDecimal getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
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

    @JsonIgnore
    public Set<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(Set<Factura> facturas) {
        this.facturas = facturas;
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombreCliente + " " + apellidos;
    }

    public void addFactura(Factura factura) {
        facturas.add(factura);
        factura.setCliente(this);
    }

    public void removeFactura(Factura factura) {
        facturas.remove(factura);
        factura.setCliente(null);
    }

    public BigDecimal calcularTotalCompras() {
        return facturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.PAGADA)
                .map(Factura::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean tieneCredito(BigDecimal monto) {
        return limiteCredito.compareTo(monto) >= 0;
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente cliente = (Cliente) o;
        return idCliente != null && idCliente.equals(cliente.idCliente);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "idCliente=" + idCliente +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", tipoCliente=" + tipoCliente +
                ", activo=" + activo +
                '}';
    }
}

