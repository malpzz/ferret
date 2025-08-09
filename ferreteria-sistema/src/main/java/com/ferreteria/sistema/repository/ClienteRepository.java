package com.ferreteria.sistema.repository;

import com.ferreteria.sistema.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Cliente
 * 
 * Proporciona métodos de acceso a datos para la gestión de clientes
 * de la ferretería.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su email
     * @param email el email del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca un cliente por su cédula
     * @param cedula la cédula del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByCedula(String cedula);

    /**
     * Obtiene todos los clientes activos
     * @return lista de clientes activos
     */
    List<Cliente> findByActivoTrue();

    /**
     * Obtiene todos los clientes inactivos
     * @return lista de clientes inactivos
     */
    List<Cliente> findByActivoFalse();

    /**
     * Obtiene clientes por tipo
     * @param tipoCliente el tipo de cliente
     * @return lista de clientes del tipo especificado
     */
    List<Cliente> findByTipoCliente(Cliente.TipoCliente tipoCliente);

    /**
     * Obtiene clientes activos por tipo
     * @param tipoCliente el tipo de cliente
     * @return lista de clientes activos del tipo especificado
     */
    List<Cliente> findByTipoClienteAndActivoTrue(Cliente.TipoCliente tipoCliente);

    /**
     * Busca clientes por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de clientes que coinciden
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "UPPER(c.nombreCliente) LIKE UPPER(CONCAT('%', :texto, '%')) OR " +
           "UPPER(c.apellidos) LIKE UPPER(CONCAT('%', :texto, '%'))")
    List<Cliente> buscarPorNombreOApellidos(@Param("texto") String texto);

    /**
     * Busca clientes activos por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de clientes activos que coinciden
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND (" +
           "UPPER(c.nombreCliente) LIKE UPPER(CONCAT('%', :texto, '%')) OR " +
           "UPPER(c.apellidos) LIKE UPPER(CONCAT('%', :texto, '%')))")
    List<Cliente> buscarActivosPorNombreOApellidos(@Param("texto") String texto);

    /**
     * Obtiene clientes con límite de crédito mayor al especificado
     * @param limite el límite mínimo
     * @return lista de clientes con crédito superior
     */
    @Query("SELECT c FROM Cliente c WHERE c.limiteCredito > :limite AND c.activo = true")
    List<Cliente> findByLimiteCreditoGreaterThan(@Param("limite") BigDecimal limite);

    /**
     * Obtiene clientes VIP con mayor volumen de compras
     * @param limite cantidad mínima de compras
     * @return lista de clientes VIP
     */
    @Query("SELECT c FROM Cliente c JOIN c.facturas f " +
           "WHERE c.tipoCliente = 'VIP' AND c.activo = true " +
           "GROUP BY c.idCliente " +
           "HAVING SUM(f.total) > :limite " +
           "ORDER BY SUM(f.total) DESC")
    List<Cliente> findClientesVIPConMayorCompras(@Param("limite") BigDecimal limite);

    /**
     * Obtiene clientes registrados en el último mes
     * @param fecha fecha límite (un mes atrás)
     * @return lista de clientes nuevos
     */
    @Query("SELECT c FROM Cliente c WHERE c.fechaRegistro >= :fecha ORDER BY c.fechaRegistro DESC")
    List<Cliente> findClientesNuevos(@Param("fecha") LocalDateTime fecha);

    /**
     * Verifica si existe un cliente con el email especificado
     * @param email el email
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un cliente con la cédula especificada
     * @param cedula la cédula
     * @return true si existe, false en caso contrario
     */
    boolean existsByCedula(String cedula);

    /**
     * Verifica si existe un cliente con el email, excluyendo un ID específico
     * @param email el email
     * @param idCliente el ID del cliente a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmailAndIdClienteNot(String email, Long idCliente);

    /**
     * Verifica si existe un cliente con la cédula, excluyendo un ID específico
     * @param cedula la cédula
     * @param idCliente el ID del cliente a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByCedulaAndIdClienteNot(String cedula, Long idCliente);

    /**
     * Cuenta clientes por tipo
     * @param tipoCliente el tipo de cliente
     * @return cantidad de clientes del tipo especificado
     */
    Long countByTipoClienteAndActivoTrue(Cliente.TipoCliente tipoCliente);

    /**
     * Obtiene clientes con mayor volumen de compras en el último año
     * @param limite cantidad mínima
     * @return lista de clientes ordenados por volumen de compras
     */
    @Query("SELECT c FROM Cliente c JOIN c.facturas f " +
           "WHERE f.fecha >= function('ADD_MONTHS', CURRENT_DATE, -12) AND f.estado = 'PAGADA' AND c.activo = true " +
           "GROUP BY c.idCliente " +
           "HAVING SUM(f.total) >= :limite " +
           "ORDER BY SUM(f.total) DESC")
    List<Cliente> findClientesMayorVolumenAnual(@Param("limite") BigDecimal limite);

    /**
     * Busca clientes con emails que coincidan con un patrón regex
     * @param patron patrón de expresión regular
     * @return lista de clientes que coinciden
     */
    @Query(value = "SELECT * FROM CLIENTES c WHERE REGEXP_LIKE(c.EMAIL, ?1, 'i')", nativeQuery = true)
    List<Cliente> findByEmailMatchingPattern(String patron);

    /**
     * Busca clientes con teléfonos que coincidan con un patrón regex
     * @param patron patrón de expresión regular
     * @return lista de clientes que coinciden
     */
    @Query(value = "SELECT * FROM CLIENTES c WHERE REGEXP_LIKE(c.TELEFONO, ?1)", nativeQuery = true)
    List<Cliente> findByTelefonoMatchingPattern(String patron);

    /**
     * Obtiene estadísticas de clientes por tipo
     * @return lista con estadísticas por tipo
     */
    @Query("SELECT c.tipoCliente, COUNT(c), AVG(c.limiteCredito) " +
           "FROM Cliente c WHERE c.activo = true " +
           "GROUP BY c.tipoCliente " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getEstadisticasPorTipo();
}

