package com.ferreteria.sistema.service;

import com.ferreteria.sistema.entity.Stock;
import com.ferreteria.sistema.entity.Producto;
import com.ferreteria.sistema.repository.StockRepository;
import com.ferreteria.sistema.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gestión de inventario y stock
 * 
 * Proporciona lógica de negocio para el manejo del inventario
 * de productos en la ferretería.
 */
@Service
@Transactional
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Obtiene todos los stocks con información de productos
     */
    @Transactional(readOnly = true)
    public List<Stock> listarTodos() {
        return stockRepository.findAllActiveOrderByProductName();
    }

    /**
     * Busca stock por ID
     */
    @Transactional(readOnly = true)
    public Optional<Stock> obtenerPorId(Long id) {
        return stockRepository.findById(id);
    }

    /**
     * Busca stock por ID de producto
     */
    @Transactional(readOnly = true)
    public Optional<Stock> obtenerPorProductoId(Long idProducto) {
        return stockRepository.findByProductoId(idProducto);
    }

    /**
     * Busca stocks por nombre de producto
     */
    @Transactional(readOnly = true)
    public List<Stock> buscarPorNombreProducto(String nombre) {
        return stockRepository.findByProductoNombreContainingIgnoreCase(nombre);
    }

    /**
     * Obtiene stocks con cantidad bajo el mínimo
     */
    @Transactional(readOnly = true)
    public List<Stock> obtenerStocksBajoMinimo() {
        return stockRepository.findStocksBajoMinimo();
    }

    /**
     * Actualiza el stock de un producto específico
     */
    public Stock actualizarStock(Long idProducto, Integer nuevaCantidad, String ubicacion) {
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (productoOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + idProducto);
        }

        Producto producto = productoOpt.get();
        Optional<Stock> stockOpt = stockRepository.findByProducto(producto);

        Stock stock;
        if (stockOpt.isPresent()) {
            // Actualizar stock existente
            stock = stockOpt.get();
            stock.setCantidad(nuevaCantidad);
            if (ubicacion != null && !ubicacion.trim().isEmpty()) {
                stock.setUbicacion(ubicacion.trim());
            }
        } else {
            // Crear nuevo registro de stock
            stock = new Stock();
            stock.setProducto(producto);
            stock.setCantidad(nuevaCantidad);
            stock.setUbicacion(ubicacion != null && !ubicacion.trim().isEmpty() 
                ? ubicacion.trim() : "ALMACEN PRINCIPAL");
        }

        return stockRepository.save(stock);
    }

    /**
     * Realiza un movimiento de entrada de stock
     */
    public Stock entradaStock(Long idProducto, Integer cantidad, String motivo) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser positiva");
        }

        Optional<Stock> stockOpt = obtenerPorProductoId(idProducto);
        Stock stock;

        if (stockOpt.isPresent()) {
            stock = stockOpt.get();
            stock.incrementarStock(cantidad);
        } else {
            // Crear stock inicial si no existe
            Optional<Producto> productoOpt = productoRepository.findById(idProducto);
            if (productoOpt.isEmpty()) {
                throw new IllegalArgumentException("Producto no encontrado");
            }
            
            stock = new Stock();
            stock.setProducto(productoOpt.get());
            stock.setCantidad(cantidad);
        }

        return stockRepository.save(stock);
    }

    /**
     * Realiza un movimiento de salida de stock
     */
    public Stock salidaStock(Long idProducto, Integer cantidad, String motivo) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser positiva");
        }

        Optional<Stock> stockOpt = obtenerPorProductoId(idProducto);
        if (stockOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe stock para el producto especificado");
        }

        Stock stock = stockOpt.get();
        if (!stock.tieneSuficienteStock(cantidad)) {
            throw new IllegalArgumentException(
                "Stock insuficiente. Disponible: " + stock.getCantidad() + 
                ", Requerido: " + cantidad
            );
        }

        stock.decrementarStock(cantidad);
        return stockRepository.save(stock);
    }

    /**
     * Verifica si hay suficiente stock para una cantidad específica
     */
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long idProducto, Integer cantidadRequerida) {
        Optional<Stock> stockOpt = obtenerPorProductoId(idProducto);
        return stockOpt.map(stock -> stock.tieneSuficienteStock(cantidadRequerida))
                      .orElse(false);
    }

    /**
     * Inicializa el stock para un producto nuevo
     */
    public Stock inicializarStock(Long idProducto, Integer cantidadInicial) {
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);
        if (productoOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        // Verificar que no exista ya un registro de stock
        if (stockRepository.existsByProductoIdProducto(idProducto)) {
            throw new IllegalArgumentException("Ya existe un registro de stock para este producto");
        }

        Stock stock = new Stock();
        stock.setProducto(productoOpt.get());
        stock.setCantidad(cantidadInicial != null ? cantidadInicial : 0);
        stock.setUbicacion("ALMACEN PRINCIPAL");

        return stockRepository.save(stock);
    }

    /**
     * Elimina un registro de stock
     */
    public void eliminar(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new IllegalArgumentException("Stock no encontrado");
        }
        stockRepository.deleteById(id);
    }

    /**
     * Obtiene estadísticas generales del stock
     */
    @Transactional(readOnly = true)
    public StockEstadisticas obtenerEstadisticas() {
        Long totalProductos = stockRepository.count();
        Long productosConStockBajo = stockRepository.countStocksBajoMinimo();
        Long valorTotalStock = stockRepository.getTotalStockValue();

        return new StockEstadisticas(
            totalProductos, 
            productosConStockBajo, 
            valorTotalStock != null ? valorTotalStock : 0L
        );
    }

    /**
     * Clase interna para estadísticas de stock
     */
    public static class StockEstadisticas {
        private final Long totalProductos;
        private final Long productosConStockBajo;
        private final Long valorTotalStock;

        public StockEstadisticas(Long totalProductos, Long productosConStockBajo, Long valorTotalStock) {
            this.totalProductos = totalProductos;
            this.productosConStockBajo = productosConStockBajo;
            this.valorTotalStock = valorTotalStock;
        }

        // Getters
        public Long getTotalProductos() { return totalProductos; }
        public Long getProductosConStockBajo() { return productosConStockBajo; }
        public Long getValorTotalStock() { return valorTotalStock; }
    }
}
