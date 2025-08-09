package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los usuarios del sistema
 * 
 * Esta entidad almacena la información de autenticación y
 * datos personales de los usuarios que acceden al sistema.
 */
@Entity
@Table(name = "USUARIOS")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDUSUARIO")
    private Long idUsuario;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 100, message = "El nombre de usuario no puede exceder 100 caracteres")
    @Column(name = "NOMBREUSUARIO", nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255, message = "La contraseña no puede exceder 255 caracteres")
    @Column(name = "CONTRASENA", nullable = false, length = 255)
    private String contraseña;

    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "NOMBRE", length = 100)
    private String nombre;

    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    @Column(name = "APELLIDOS", length = 100)
    private String apellidos;

    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    @Column(name = "TELEFONO", length = 15)
    private String telefono;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "ULTIMO_ACCESO")
    private LocalDateTime ultimoAcceso;

    @Column(name = "INTENTOS_FALLIDOS", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación muchos a uno con rol
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDROL", nullable = false)
    private Rol rol;

    // Relación uno a muchos con facturas
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Factura> facturas = new HashSet<>();

    // Relación uno a muchos con pedidos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Pedido> pedidos = new HashSet<>();

    // Constructor por defecto
    public Usuario() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Usuario(String nombreUsuario, String contraseña, String email, String nombre, String apellidos) {
        this();
        this.nombreUsuario = nombreUsuario;
        this.contraseña = contraseña;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Set<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(Set<Factura> facturas) {
        this.facturas = facturas;
    }

    public Set<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(Set<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
    }

    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
    }

    public void reiniciarIntentosFallidos() {
        this.intentosFallidos = 0;
    }

    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return idUsuario != null && idUsuario.equals(usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}

