package com.ferreteria.sistema.repository;

import com.ferreteria.sistema.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Producto
 * 
 * Proporciona métodos de acceso a datos para la gestión del catálogo
 * de productos de la ferretería.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su código
     * @param codigoProducto el código del producto
     * @return Optional con el producto si existe
     */
    Optional<Producto> findByCodigoProducto(String codigoProducto);

    /**
     * Obtiene todos los productos activos
     * @return lista de productos activos
     */
    List<Producto> findByActivoTrue();

    /**
     * Obtiene todos los productos inactivos
     * @return lista de productos inactivos
     */
    List<Producto> findByActivoFalse();

    /**
     * Obtiene productos por categoría
     * @param categoria la categoría
     * @return lista de productos de la categoría especificada
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Obtiene productos activos por categoría
     * @param categoria la categoría
     * @return lista de productos activos de la categoría
     */
    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    /**
     * Obtiene productos por marca
     * @param marca la marca
     * @return lista de productos de la marca especificada
     */
    List<Producto> findByMarca(String marca);

    /**
     * Obtiene productos activos por marca
     * @param marca la marca
     * @return lista de productos activos de la marca
     */
    List<Producto> findByMarcaAndActivoTrue(String marca);

    /**
     * Obtiene productos por proveedor
     * @param idProveedor el ID del proveedor
     * @return lista de productos del proveedor
     */
    @Query("SELECT p FROM Producto p WHERE p.proveedor.idProveedor = :idProveedor")
    List<Producto> findByProveedorId(@Param("idProveedor") Long idProveedor);

    /**
     * Obtiene productos activos por proveedor
     * @param idProveedor el ID del proveedor
     * @return lista de productos activos del proveedor
     */
    @Query("SELECT p FROM Producto p WHERE p.proveedor.idProveedor = :idProveedor AND p.activo = true")
    List<Producto> findByProveedorIdAndActivoTrue(@Param("idProveedor") Long idProveedor);

    /**
     * Busca productos por nombre
     * @param nombre texto a buscar en el nombre
     * @return lista de productos que coinciden
     */
    @Query("SELECT p FROM Producto p WHERE UPPER(p.nombreProducto) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca productos activos por nombre
     * @param nombre texto a buscar en el nombre
     * @return lista de productos activos que coinciden
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "UPPER(p.nombreProducto) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Producto> findActivosByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Obtiene productos con precio en un rango específico
     * @param precioMin precio mínimo
     * @param precioMax precio máximo
     * @return lista de productos en el rango de precio
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax AND p.activo = true")
    List<Producto> findByPrecioBetween(@Param("precioMin") BigDecimal precioMin, 
                                      @Param("precioMax") BigDecimal precioMax);

    /**
     * Obtiene productos con stock bajo el mínimo
     * @return lista de productos que requieren reabastecimiento
     */
    @Query("SELECT p FROM Producto p JOIN p.stock s " +
           "WHERE s.cantidad <= p.stockMinimo AND p.activo = true " +
           "ORDER BY (s.cantidad - p.stockMinimo) ASC")
    List<Producto> findProductosConStockBajo();

    /**
     * Obtiene productos sin stock
     * @return lista de productos agotados
     */
    @Query("SELECT p FROM Producto p JOIN p.stock s " +
           "WHERE s.cantidad = 0 AND p.activo = true")
    List<Producto> findProductosAgotados();

    /**
     * Obtiene productos más vendidos en el último período
     * @param limite cantidad de productos a retornar
     * @return lista de productos más vendidos
     */
    @Query("SELECT p FROM Producto p JOIN p.detallesFactura df JOIN df.factura f " +
           "WHERE f.fecha >= function('ADD_MONTHS', CURRENT_DATE, -1) AND f.estado = 'PAGADA' " +
           "GROUP BY p.idProducto " +
           "ORDER BY SUM(df.cantidad) DESC")
    List<Producto> findProductosMasVendidos(@Param("limite") int limite);

    /**
     * Verifica si existe un producto con el código especificado
     * @param codigoProducto el código del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigoProducto(String codigoProducto);

    /**
     * Verifica si existe un producto con el código, excluyendo un ID específico
     * @param codigoProducto el código del producto
     * @param idProducto el ID del producto a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigoProductoAndIdProductoNot(String codigoProducto, Long idProducto);

    /**
     * Cuenta productos por categoría
     * @param categoria la categoría
     * @return cantidad de productos activos en la categoría
     */
    Long countByCategoriaAndActivoTrue(String categoria);

    /**
     * Cuenta productos por marca
     * @param marca la marca
     * @return cantidad de productos activos de la marca
     */
    Long countByMarcaAndActivoTrue(String marca);

    /**
     * Obtiene todas las categorías disponibles
     * @return lista de categorías únicas
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.activo = true ORDER BY p.categoria")
    List<String> findDistinctCategorias();

    /**
     * Obtiene todas las marcas disponibles
     * @return lista de marcas únicas
     */
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.activo = true AND p.marca IS NOT NULL ORDER BY p.marca")
    List<String> findDistinctMarcas();

    /**
     * Busca productos con códigos que coincidan con un patrón regex
     * @param patron patrón de expresión regular
     * @return lista de productos que coinciden
     */
    @Query(value = "SELECT * FROM PRODUCTOS p WHERE REGEXP_LIKE(p.CODIGO_PRODUCTO, ?1, 'i')", nativeQuery = true)
    List<Producto> findByCodigoMatchingPattern(String patron);

    /**
     * Obtiene productos con mayor margen de ganancia
     * @param limite cantidad de productos a retornar
     * @return lista de productos ordenados por margen
     */
    @Query("SELECT p FROM Producto p WHERE p.precioCompra IS NOT NULL AND p.activo = true " +
           "ORDER BY ((p.precio - p.precioCompra) / p.precioCompra) DESC")
    List<Producto> findProductosMayorMargen(@Param("limite") int limite);

    /**
     * Busca productos por múltiples criterios
     * @param nombre texto en el nombre
     * @param categoria categoría del producto
     * @param marca marca del producto
     * @return lista de productos que coinciden
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND (:nombre IS NULL OR UPPER(p.nombreProducto) LIKE UPPER(CONCAT('%', :nombre, '%'))) " +
           "AND (:categoria IS NULL OR p.categoria = :categoria) " +
           "AND (:marca IS NULL OR p.marca = :marca)")
    List<Producto> buscarPorCriteriosMultiples(@Param("nombre") String nombre,
                                               @Param("categoria") String categoria,
                                               @Param("marca") String marca);

    /**
     * Obtiene estadísticas de productos por categoría
     * @return lista con estadísticas por categoría
     */
    @Query("SELECT p.categoria, COUNT(p), AVG(p.precio), MIN(p.precio), MAX(p.precio) " +
           "FROM Producto p WHERE p.activo = true " +
           "GROUP BY p.categoria " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getEstadisticasPorCategoria();
}

