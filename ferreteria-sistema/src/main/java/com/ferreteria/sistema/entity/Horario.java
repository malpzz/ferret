package com.ferreteria.sistema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los horarios laborales de empleados
 * 
 * Esta entidad gestiona la programación de horarios de trabajo
 * de los empleados de la ferretería.
 */
@Entity
@Table(name = "HORARIOS", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"IDEMPLEADO", "FECHA"}))
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDHORARIO")
    private Long idHorario;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de entrada es obligatoria")
    @DecimalMin(value = "0.0", message = "La hora de entrada debe ser mayor o igual a 0")
    @DecimalMax(value = "23.99", message = "La hora de entrada debe ser menor a 24")
    @Column(name = "HORA_ENTRADA", nullable = false, precision = 4, scale = 2)
    private BigDecimal horaEntrada;

    @NotNull(message = "La hora de salida es obligatoria")
    @DecimalMin(value = "0.0", message = "La hora de salida debe ser mayor o igual a 0")
    @DecimalMax(value = "23.99", message = "La hora de salida debe ser menor a 24")
    @Column(name = "HORA_SALIDA", nullable = false, precision = 4, scale = 2)
    private BigDecimal horaSalida;

    @Size(max = 200, message = "Las observaciones no pueden exceder 200 caracteres")
    @Column(name = "OBSERVACIONES", length = 200)
    private String observaciones;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación muchos a uno con empleado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDEMPLEADO", nullable = false)
    private Empleado empleado;

    // Columna calculada en BD (no actualizable)
    @Column(name = "HORAS_TRABAJADAS", precision = 4, scale = 2, insertable = false, updatable = false)
    private BigDecimal horasTrabajadas;

    // Constructor por defecto
    public Horario() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public Horario(LocalDate fecha, BigDecimal horaEntrada, BigDecimal horaSalida, Empleado empleado) {
        this();
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.empleado = empleado;
    }

    // Getters y Setters
    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Long idHorario) {
        this.idHorario = idHorario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(BigDecimal horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public BigDecimal getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(BigDecimal horaSalida) {
        this.horaSalida = horaSalida;
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

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    // Conveniencia para DAO: id del empleado
    public Long getIdEmpleado() {
        return empleado != null ? empleado.getIdEmpleado() : null;
    }

    public void setIdEmpleado(Long idEmpleado) {
        if (this.empleado == null) {
            this.empleado = new Empleado();
        }
        this.empleado.setIdEmpleado(idEmpleado);
    }

    public BigDecimal getHorasTrabajadas() {
        return horasTrabajadas != null ? horasTrabajadas : calcularHorasTrabajadas();
    }

    public void setHorasTrabajadas(BigDecimal horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    // Métodos de utilidad
    public BigDecimal calcularHorasTrabajadas() {
        if (horaSalida.compareTo(horaEntrada) >= 0) {
            // Jornada normal
            return horaSalida.subtract(horaEntrada);
        } else {
            // Jornada nocturna (pasa de medianoche)
            return BigDecimal.valueOf(24).subtract(horaEntrada).add(horaSalida);
        }
    }

    public String getHoraEntradaFormateada() {
        return formatearHora(horaEntrada);
    }

    public String getHoraSalidaFormateada() {
        return formatearHora(horaSalida);
    }

    public String getHorasTrabajadasFormateadas() {
        return formatearHora(calcularHorasTrabajadas());
    }

    private String formatearHora(BigDecimal hora) {
        int horas = hora.intValue();
        int minutos = hora.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(60)).intValue();
        return String.format("%02d:%02d", horas, minutos);
    }

    public boolean esJornadaCompleta() {
        return calcularHorasTrabajadas().compareTo(BigDecimal.valueOf(8)) >= 0;
    }

    public boolean esHoraExtra() {
        return calcularHorasTrabajadas().compareTo(BigDecimal.valueOf(8)) > 0;
    }

    public BigDecimal getHorasExtra() {
        BigDecimal horasTrabajadas = calcularHorasTrabajadas();
        if (horasTrabajadas.compareTo(BigDecimal.valueOf(8)) > 0) {
            return horasTrabajadas.subtract(BigDecimal.valueOf(8));
        }
        return BigDecimal.ZERO;
    }

    public boolean esFechaValida() {
        return fecha != null && !fecha.isAfter(LocalDate.now());
    }

    public boolean esHorarioValido() {
        return horaEntrada != null && horaSalida != null && 
               !horaEntrada.equals(horaSalida) &&
               horaEntrada.compareTo(BigDecimal.valueOf(24)) < 0 &&
               horaSalida.compareTo(BigDecimal.valueOf(24)) < 0;
    }

    // Métodos equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Horario)) return false;
        Horario horario = (Horario) o;
        return idHorario != null && idHorario.equals(horario.idHorario);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Horario{" +
                "idHorario=" + idHorario +
                ", fecha=" + fecha +
                ", horaEntrada=" + getHoraEntradaFormateada() +
                ", horaSalida=" + getHoraSalidaFormateada() +
                ", horasTrabajadas=" + getHorasTrabajadasFormateadas() +
                '}';
    }
}

