package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Stock;
import com.ferreteria.sistema.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión de stock/inventario
 * 
 * Maneja las operaciones CRUD y funcionalidades específicas
 * para el control de inventario.
 */
@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockRestController {

    @Autowired
    private StockService stockService;

    /**
     * Lista todos los stocks con información de productos
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Stock>> listar() {
        try {
            List<Stock> stocks = stockService.listarTodos();
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            System.out.println("ERROR listando stocks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene un stock específico por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            Optional<Stock> stock = stockService.obtenerPorId(id);
            if (stock.isPresent()) {
                return ResponseEntity.ok(stock.get());
            } else {
                return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Stock no encontrado"));
            }
        } catch (Exception e) {
            System.out.println("ERROR obteniendo stock: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("mensaje", "Error interno del servidor"));
        }
    }

    /**
     * Obtiene stock por ID de producto
     */
    @GetMapping("/producto/{idProducto}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> obtenerPorProducto(@PathVariable Long idProducto) {
        try {
            Optional<Stock> stock = stockService.obtenerPorProductoId(idProducto);
            if (stock.isPresent()) {
                return ResponseEntity.ok(stock.get());
            } else {
                return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Stock no encontrado para el producto"));
            }
        } catch (Exception e) {
            System.out.println("ERROR obteniendo stock por producto: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("mensaje", "Error interno del servidor"));
        }
    }

    /**
     * Busca stocks por nombre de producto
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Stock>> buscar(@RequestParam String nombre) {
        try {
            List<Stock> stocks = stockService.buscarPorNombreProducto(nombre);
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            System.out.println("ERROR buscando stocks: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene stocks con cantidad bajo el mínimo
     */
    @GetMapping("/bajo-minimo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Stock>> stocksBajoMinimo() {
        try {
            List<Stock> stocks = stockService.obtenerStocksBajoMinimo();
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            System.out.println("ERROR obteniendo stocks bajo mínimo: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Actualiza el stock de un producto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ActualizarStockRequest request) {
        try {
            System.out.println("=== DEBUG ACTUALIZAR STOCK ===");
            System.out.println("ID: " + id);
            System.out.println("Nueva cantidad: " + request.getCantidad());
            System.out.println("Ubicación: " + request.getUbicacion());

            Optional<Stock> stockOpt = stockService.obtenerPorId(id);
            if (stockOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Stock no encontrado"));
            }

            Stock stock = stockOpt.get();
            Stock stockActualizado = stockService.actualizarStock(
                stock.getProducto().getIdProducto(),
                request.getCantidad(),
                request.getUbicacion()
            );

            return ResponseEntity.ok(stockActualizado);
        } catch (Exception e) {
            System.out.println("ERROR actualizando stock: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("mensaje", "Error actualizando stock: " + e.getMessage()));
        }
    }

    /**
     * Realiza un movimiento de stock (entrada o salida)
     */
    @PostMapping("/movimiento")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> movimiento(@RequestBody MovimientoStockRequest request) {
        try {
            System.out.println("=== DEBUG MOVIMIENTO STOCK ===");
            System.out.println("ID Producto: " + request.getIdProducto());
            System.out.println("Tipo: " + request.getTipo());
            System.out.println("Cantidad: " + request.getCantidad());
            System.out.println("Motivo: " + request.getMotivo());

            Stock stock;
            if ("entrada".equalsIgnoreCase(request.getTipo())) {
                stock = stockService.entradaStock(
                    request.getIdProducto(),
                    request.getCantidad(),
                    request.getMotivo()
                );
            } else if ("salida".equalsIgnoreCase(request.getTipo())) {
                stock = stockService.salidaStock(
                    request.getIdProducto(),
                    request.getCantidad(),
                    request.getMotivo()
                );
            } else {
                return ResponseEntity.status(400)
                    .body(Map.of("mensaje", "Tipo de movimiento inválido. Use 'entrada' o 'salida'"));
            }

            return ResponseEntity.ok(Map.of(
                "mensaje", "Movimiento de stock registrado correctamente",
                "stock", stock
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERROR en movimiento de stock: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("mensaje", "Error interno del servidor"));
        }
    }

    /**
     * Inicializa stock para un producto
     */
    @PostMapping("/inicializar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> inicializarStock(@RequestBody InicializarStockRequest request) {
        try {
            Stock stock = stockService.inicializarStock(
                request.getIdProducto(),
                request.getCantidadInicial()
            );
            return ResponseEntity.ok(Map.of(
                "mensaje", "Stock inicializado correctamente",
                "stock", stock
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERROR inicializando stock: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("mensaje", "Error interno del servidor"));
        }
    }

    /**
     * Verifica disponibilidad de stock
     */
    @GetMapping("/disponibilidad/{idProducto}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @PathVariable Long idProducto,
            @RequestParam Integer cantidad) {
        try {
            boolean disponible = stockService.verificarDisponibilidad(idProducto, cantidad);
            Optional<Stock> stockOpt = stockService.obtenerPorProductoId(idProducto);
            
            Map<String, Object> respuesta = Map.of(
                "disponible", disponible,
                "stockActual", stockOpt.map(Stock::getCantidad).orElse(0),
                "cantidadRequerida", cantidad
            );
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("ERROR verificando disponibilidad: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene estadísticas generales del stock
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<StockService.StockEstadisticas> obtenerEstadisticas() {
        try {
            StockService.StockEstadisticas estadisticas = stockService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.out.println("ERROR obteniendo estadísticas: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Elimina un registro de stock
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            stockService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Stock eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERROR eliminando stock: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("mensaje", "Error interno del servidor"));
        }
    }

    // DTOs para requests
    public static class ActualizarStockRequest {
        private Integer cantidad;
        private String ubicacion;

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public String getUbicacion() { return ubicacion; }
        public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    }

    public static class MovimientoStockRequest {
        private Long idProducto;
        private String tipo; // "entrada" o "salida"
        private Integer cantidad;
        private String motivo;

        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }

    public static class InicializarStockRequest {
        private Long idProducto;
        private Integer cantidadInicial;

        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        public Integer getCantidadInicial() { return cantidadInicial; }
        public void setCantidadInicial(Integer cantidadInicial) { this.cantidadInicial = cantidadInicial; }
    }
}
