package com.ferreteria.sistema.repository;

import com.ferreteria.sistema.entity.Stock;
import com.ferreteria.sistema.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operaciones CRUD sobre la entidad Stock
 * 
 * Proporciona métodos para gestionar el inventario de productos
 * en la ferretería.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Busca el registro de stock por producto
     */
    Optional<Stock> findByProducto(Producto producto);

    /**
     * Busca el registro de stock por ID de producto
     */
    @Query("SELECT s FROM Stock s WHERE s.producto.idProducto = :idProducto")
    Optional<Stock> findByProductoId(@Param("idProducto") Long idProducto);

    /**
     * Obtiene todos los stocks con cantidad por debajo del mínimo
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.producto p WHERE s.cantidad <= p.stockMinimo AND p.activo = true")
    List<Stock> findStocksBajoMinimo();

    /**
     * Busca stocks por ubicación
     */
    List<Stock> findByUbicacionContainingIgnoreCase(String ubicacion);

    /**
     * Obtiene stocks de productos activos ordenados por nombre de producto
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.producto p WHERE p.activo = true ORDER BY p.nombreProducto")
    List<Stock> findAllActiveOrderByProductName();

    /**
     * Busca stocks por nombre de producto (búsqueda parcial)
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.producto p WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.activo = true")
    List<Stock> findByProductoNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Obtiene el stock total de todos los productos activos
     */
    @Query("SELECT SUM(s.cantidad) FROM Stock s WHERE s.producto.activo = true")
    Long getTotalStockValue();

    /**
     * Cuenta cuántos productos tienen stock bajo mínimo
     */
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.cantidad <= s.producto.stockMinimo AND s.producto.activo = true")
    Long countStocksBajoMinimo();

    /**
     * Verifica si existe stock para un producto específico
     */
    boolean existsByProductoIdProducto(Long idProducto);
}
