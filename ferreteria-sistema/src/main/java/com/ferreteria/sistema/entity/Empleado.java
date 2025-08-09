package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los empleados de la ferretería
 * 
 * Esta entidad almacena la información personal y laboral
 * de todos los empleados que trabajan en la ferretería.
 */
@Entity
@Table(name = "EMPLEADOS")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDEMPLEADO")
    private Long idEmpleado;

    @NotBlank(message = "El nombre del empleado es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "NOMBREEMPLEADO", nullable = false, length = 100)
    private String nombreEmpleado;

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

    @NotBlank(message = "La cédula es obligatoria")
    @Size(max = 20, message = "La cédula no puede exceder 20 caracteres")
    @Column(name = "CEDULA", nullable = false, unique = true, length = 20)
    private String cedula;

    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 100, message = "El puesto no puede exceder 100 caracteres")
    @Column(name = "PUESTO", nullable = false, length = 100)
    private String puesto;

    @Positive(message = "El salario debe ser mayor que cero")
    @Column(name = "SALARIO", precision = 10, scale = 2)
    private BigDecimal salario;

    @Column(name = "FECHA_INGRESO")
    private LocalDate fechaIngreso;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @Column(name = "FECHA_MODIFICACION")
    private LocalDateTime fechaModificacion;

    // Relación uno a muchos con horarios
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Horario> horarios = new HashSet<>();

    // Constructor por defecto
    public Empleado() {
        this.fechaIngreso = LocalDate.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Empleado(String nombreEmpleado, String apellidos, String direccion, String telefono, String cedula, String puesto) {
        this();
        this.nombreEmpleado = nombreEmpleado;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.telefono = telefono;
        this.cedula = cedula;
        this.puesto = puesto;
    }

    // Métodos del ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
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

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Set<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(Set<Horario> horarios) {
        this.horarios = horarios;
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombreEmpleado + " " + apellidos;
    }

    public void addHorario(Horario horario) {
        horarios.add(horario);
        horario.setEmpleado(this);
    }

    public void removeHorario(Horario horario) {
        horarios.remove(horario);
        horario.setEmpleado(null);
    }

    public long getDiasAntiguedad() {
        return LocalDate.now().toEpochDay() - fechaIngreso.toEpochDay();
    }

    public int getAñosAntiguedad() {
        return (int) (getDiasAntiguedad() / 365);
    }

    public boolean esNuevo() {
        return getDiasAntiguedad() <= 90; // Menos de 3 meses
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empleado)) return false;
        Empleado empleado = (Empleado) o;
        return idEmpleado != null && idEmpleado.equals(empleado.idEmpleado);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "idEmpleado=" + idEmpleado +
                ", nombreEmpleado='" + nombreEmpleado + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", cedula='" + cedula + '\'' +
                ", puesto='" + puesto + '\'' +
                ", activo=" + activo +
                '}';
    }
}

