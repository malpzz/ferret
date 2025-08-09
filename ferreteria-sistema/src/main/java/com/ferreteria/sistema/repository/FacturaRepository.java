package com.ferreteria.sistema.repository;

import com.ferreteria.sistema.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Factura
 * 
 * Proporciona métodos de acceso a datos para la gestión de facturas
 * de ventas de la ferretería.
 */
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    /**
     * Busca una factura por su número
     * @param numeroFactura el número de la factura
     * @return Optional con la factura si existe
     */
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    /**
     * Obtiene facturas por estado
     * @param estado el estado de la factura
     * @return lista de facturas con el estado especificado
     */
    List<Factura> findByEstado(Factura.EstadoFactura estado);

    /**
     * Obtiene facturas por método de pago
     * @param metodoPago el método de pago
     * @return lista de facturas con el método de pago especificado
     */
    List<Factura> findByMetodoPago(Factura.MetodoPago metodoPago);

    /**
     * Obtiene facturas por cliente
     * @param idCliente el ID del cliente
     * @return lista de facturas del cliente
     */
    @Query("SELECT f FROM Factura f WHERE f.cliente.idCliente = :idCliente ORDER BY f.fecha DESC")
    List<Factura> findByClienteId(@Param("idCliente") Long idCliente);

    /**
     * Obtiene facturas por usuario
     * @param idUsuario el ID del usuario
     * @return lista de facturas creadas por el usuario
     */
    @Query("SELECT f FROM Factura f WHERE f.usuario.idUsuario = :idUsuario ORDER BY f.fecha DESC")
    List<Factura> findByUsuarioId(@Param("idUsuario") Long idUsuario);

    /**
     * Obtiene facturas en un rango de fechas
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return lista de facturas en el rango
     */
    @Query("SELECT f FROM Factura f WHERE f.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY f.fecha DESC")
    List<Factura> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                    @Param("fechaFin") LocalDate fechaFin);

    /**
     * Obtiene facturas pagadas en un rango de fechas
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return lista de facturas pagadas en el rango
     */
    @Query("SELECT f FROM Factura f WHERE f.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "AND f.estado = 'PAGADA' ORDER BY f.fecha DESC")
    List<Factura> findFacturasPagadasEnRango(@Param("fechaInicio") LocalDate fechaInicio, 
                                            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Obtiene facturas con total mayor al especificado
     * @param total total mínimo
     * @return lista de facturas con total superior
     */
    @Query("SELECT f FROM Factura f WHERE f.total > :total ORDER BY f.total DESC")
    List<Factura> findByTotalGreaterThan(@Param("total") BigDecimal total);

    /**
     * Obtiene facturas del día actual
     * @return lista de facturas de hoy
     */
    @Query("SELECT f FROM Factura f WHERE f.fecha = CURRENT_DATE ORDER BY f.fechaCreacion DESC")
    List<Factura> findFacturasDeHoy();

    /**
     * Obtiene facturas del mes actual
     * @return lista de facturas del mes
     */
    @Query("SELECT f FROM Factura f WHERE EXTRACT(MONTH FROM f.fecha) = EXTRACT(MONTH FROM CURRENT_DATE) " +
           "AND EXTRACT(YEAR FROM f.fecha) = EXTRACT(YEAR FROM CURRENT_DATE) " +
           "ORDER BY f.fecha DESC")
    List<Factura> findFacturasDelMes();

    /**
     * Verifica si existe una factura con el número especificado
     * @param numeroFactura el número de la factura
     * @return true si existe, false en caso contrario
     */
    boolean existsByNumeroFactura(String numeroFactura);

    /**
     * Calcula el total de ventas en un rango de fechas
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return total de ventas pagadas
     */
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f " +
           "WHERE f.fecha BETWEEN :fechaInicio AND :fechaFin AND f.estado = 'PAGADA'")
    BigDecimal calcularTotalVentasEnRango(@Param("fechaInicio") LocalDate fechaInicio, 
                                         @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcula el total de ventas del día
     * @return total de ventas de hoy
     */
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f " +
           "WHERE f.fecha = CURRENT_DATE AND f.estado = 'PAGADA'")
    BigDecimal calcularVentasDelDia();

    /**
     * Calcula el total de ventas del mes
     * @return total de ventas del mes
     */
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f " +
           "WHERE EXTRACT(MONTH FROM f.fecha) = EXTRACT(MONTH FROM CURRENT_DATE) " +
           "AND EXTRACT(YEAR FROM f.fecha) = EXTRACT(YEAR FROM CURRENT_DATE) " +
           "AND f.estado = 'PAGADA'")
    BigDecimal calcularVentasDelMes();

    /**
     * Cuenta facturas por estado
     * @param estado el estado de la factura
     * @return cantidad de facturas en el estado
     */
    Long countByEstado(Factura.EstadoFactura estado);

    /**
     * Cuenta facturas del día por estado
     * @param estado el estado de la factura
     * @return cantidad de facturas de hoy en el estado
     */
    @Query("SELECT COUNT(f) FROM Factura f WHERE f.fecha = CURRENT_DATE AND f.estado = :estado")
    Long countFacturasDelDiaPorEstado(@Param("estado") Factura.EstadoFactura estado);

    /**
     * Obtiene clientes con mayor volumen de compras
     * @param limite cantidad de clientes a retornar
     * @return lista de información de clientes y totales
     */
    @Query("SELECT f.cliente, SUM(f.total), COUNT(f) FROM Factura f " +
           "WHERE f.estado = 'PAGADA' " +
           "GROUP BY f.cliente " +
           "ORDER BY SUM(f.total) DESC")
    List<Object[]> findClientesMayorVolumen(@Param("limite") int limite);

    /**
     * Obtiene estadísticas de ventas por método de pago
     * @return lista con estadísticas por método de pago
     */
    @Query("SELECT f.metodoPago, COUNT(f), SUM(f.total), AVG(f.total) FROM Factura f " +
           "WHERE f.estado = 'PAGADA' " +
           "GROUP BY f.metodoPago " +
           "ORDER BY SUM(f.total) DESC")
    List<Object[]> getEstadisticasPorMetodoPago();

    /**
     * Obtiene ventas diarias de los últimos días
     * @param dias cantidad de días hacia atrás
     * @return lista con fechas y totales diarios
     */
    @Query(value = "SELECT TRUNC(f.fecha) AS FECHA, SUM(f.total) AS TOTAL, COUNT(1) AS NUM " +
            "FROM FACTURA f " +
            "WHERE TRUNC(f.fecha) >= TRUNC(SYSDATE) - :dias AND f.estado = 'PAGADA' " +
            "GROUP BY TRUNC(f.fecha) " +
            "ORDER BY TRUNC(f.fecha) DESC", nativeQuery = true)
    List<Object[]> getVentasDiarias(@Param("dias") int dias);

    /**
     * Obtiene facturas pendientes de pago
     * @return lista de facturas pendientes
     */
    @Query("SELECT f FROM Factura f WHERE f.estado = 'PENDIENTE' ORDER BY f.fecha ASC")
    List<Factura> findFacturasPendientes();

    /**
     * Obtiene facturas con mayor tiempo pendiente
     * @param dias cantidad de días de antigüedad
     * @return lista de facturas pendientes antiguas
     */
    @Query(value = "SELECT * FROM FACTURA f WHERE f.estado = 'PENDIENTE' " +
            "AND TRUNC(f.fecha) <= TRUNC(SYSDATE) - :dias ORDER BY f.fecha ASC",
            nativeQuery = true)
    List<Factura> findFacturasPendientesAntiguas(@Param("dias") int dias);

    /**
     * Obtiene el promedio de ventas diarias del último mes
     * @return promedio de ventas diarias
     */
    @Query(value = "SELECT AVG(daily_total) FROM (" +
            "SELECT SUM(f.total) as daily_total FROM FACTURA f " +
            "WHERE f.fecha >= TRUNC(CURRENT_DATE) - 30 AND f.estado = 'PAGADA' " +
            "GROUP BY f.fecha)", nativeQuery = true)
    BigDecimal getPromedioVentasDiarias();

    /**
     * Busca facturas por número parcial
     * @param numeroFactura texto a buscar en el número
     * @return lista de facturas que coinciden
     */
    @Query("SELECT f FROM Factura f WHERE UPPER(f.numeroFactura) LIKE UPPER(CONCAT('%', :numeroFactura, '%'))")
    List<Factura> findByNumeroFacturaContaining(@Param("numeroFactura") String numeroFactura);
}

