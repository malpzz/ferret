package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Factura;
import com.ferreteria.sistema.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaRestController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<List<Factura>> listar() { 
        List<Factura> facturas = facturaService.obtenerTodos();
        System.out.println("DEBUG - Enviando " + facturas.size() + " facturas al frontend");
        for (Factura f : facturas) {
            String clienteInfo = f.getCliente() != null ? f.getCliente().getNombreCliente() : "NULL";
            String usuarioInfo = f.getUsuario() != null ? 
                (f.getUsuario().getNombre() != null && f.getUsuario().getApellidos() != null ?
                 f.getUsuario().getNombre() + " " + f.getUsuario().getApellidos() :
                 f.getUsuario().getNombreUsuario()) : "NULL";
            System.out.println("Factura: " + f.getIdFactura() + 
                             ", Cliente: " + clienteInfo + 
                             ", Usuario: " + usuarioInfo);
        }
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        Optional<Factura> facturaOpt = facturaService.obtenerPorId(id);
        if (facturaOpt.isPresent()) {
            Factura factura = facturaOpt.get();
            String clienteInfo = factura.getCliente() != null ? 
                factura.getCliente().getNombreCliente() + " (ID: " + factura.getCliente().getIdCliente() + ")" : "NULL";
            String usuarioInfo = factura.getUsuario() != null ? 
                (factura.getUsuario().getNombre() != null && factura.getUsuario().getApellidos() != null ?
                 factura.getUsuario().getNombre() + " " + factura.getUsuario().getApellidos() :
                 factura.getUsuario().getNombreUsuario()) + " (ID: " + factura.getUsuario().getIdUsuario() + ")" : "NULL";
            System.out.println("DEBUG - Enviando factura " + id + 
                             ", Cliente: " + clienteInfo + 
                             ", Usuario: " + usuarioInfo);
            return ResponseEntity.ok(factura);
        }
        return ResponseEntity.notFound().build();
    }

    public static class CrearFacturaRequest {
        public String numero;
        public String fecha; // Cambiar a String para recibir formato "YYYY-MM-DD"
        public Long idCliente;
        public String metodoPago;
        public String estado;
        public String observaciones;
        public List<ProductoFacturaRequest> productos;
    }

    public static class ProductoFacturaRequest {
        public Long idProducto;
        public BigDecimal precio;
        public Integer cantidad;
        public BigDecimal descuento;

        public ProductoFacturaRequest() {}
        
        public ProductoFacturaRequest(Long idProducto, BigDecimal precio, Integer cantidad, BigDecimal descuento) {
            this.idProducto = idProducto;
            this.precio = precio;
            this.cantidad = cantidad;
            this.descuento = descuento;
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<?> crear(@RequestBody CrearFacturaRequest req) {
        System.out.println("=== DEBUG CREAR FACTURA ===");
        System.out.println("Numero: " + req.numero);
        System.out.println("Fecha: " + req.fecha);
        System.out.println("IdCliente: " + req.idCliente);
        System.out.println("MetodoPago: " + req.metodoPago);
        System.out.println("Estado: " + req.estado);
        System.out.println("Observaciones: " + req.observaciones);
        System.out.println("Productos: " + (req.productos != null ? req.productos.size() : "0"));
        
        try {
            // Validar campos obligatorios
            if (req.numero == null || req.numero.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "El número de factura es obligatorio"));
            }
            if (req.fecha == null || req.fecha.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "La fecha es obligatoria"));
            }
            if (req.idCliente == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "El cliente es obligatorio"));
            }
            
            // Convertir fecha de String a Date
            Date fechaDate;
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                fechaDate = formatter.parse(req.fecha);
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "Formato de fecha inválido. Use YYYY-MM-DD"));
            }
            
            // Convertir productos si existen
            List<FacturaService.DetalleFacturaRequest> detalles = null;
            if (req.productos != null && !req.productos.isEmpty()) {
                System.out.println("DEBUG - Procesando " + req.productos.size() + " productos recibidos para crear:");
                for (int i = 0; i < req.productos.size(); i++) {
                    ProductoFacturaRequest p = req.productos.get(i);
                    System.out.println("  Producto " + i + ": ID=" + p.idProducto + 
                                     ", Precio=" + p.precio + ", Cantidad=" + p.cantidad + 
                                     ", Descuento=" + p.descuento);
                }
                
                detalles = req.productos.stream()
                    .filter(p -> p.idProducto != null) // Filtrar productos con ID null
                    .map(p -> new FacturaService.DetalleFacturaRequest(
                        p.idProducto, p.precio, p.cantidad, p.descuento != null ? p.descuento : BigDecimal.ZERO))
                    .collect(java.util.stream.Collectors.toList());
                    
                System.out.println("DEBUG - Productos válidos después del filtro: " + detalles.size());
            }
            
            Long id = facturaService.crearFacturaCompleta(req.numero, fechaDate, req.idCliente, 
                req.metodoPago != null ? req.metodoPago : "EFECTIVO", 
                req.estado != null ? req.estado : "PENDIENTE", 
                req.observaciones, detalles);
                
            return ResponseEntity.ok(Map.of("id", id, "mensaje", "Factura creada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Validación", "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "mensaje", "Error al crear la factura: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody CrearFacturaRequest req) {
        System.out.println("=== DEBUG ACTUALIZAR FACTURA ID: " + id + " ===");
        System.out.println("Numero: " + req.numero);
        System.out.println("Fecha: " + req.fecha);
        System.out.println("IdCliente: " + req.idCliente);
        System.out.println("MetodoPago: " + req.metodoPago);
        System.out.println("Estado: " + req.estado);
        System.out.println("Observaciones: " + req.observaciones);
        System.out.println("Productos: " + (req.productos != null ? req.productos.size() : "0"));
        
        try {
            // Verificar que la factura existe
            if (!facturaService.obtenerPorId(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Validar campos obligatorios
            if (req.numero == null || req.numero.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "El número de factura es obligatorio"));
            }
            if (req.fecha == null || req.fecha.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "La fecha es obligatoria"));
            }
            if (req.idCliente == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "El cliente es obligatorio"));
            }
            
            // Convertir fecha de String a Date
            Date fechaDate;
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                fechaDate = formatter.parse(req.fecha);
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Validación", "mensaje", "Formato de fecha inválido. Use YYYY-MM-DD"));
            }
            
            // Convertir productos si existen
            List<FacturaService.DetalleFacturaRequest> detalles = null;
            if (req.productos != null && !req.productos.isEmpty()) {
                System.out.println("DEBUG - Procesando " + req.productos.size() + " productos recibidos para actualizar:");
                for (int i = 0; i < req.productos.size(); i++) {
                    ProductoFacturaRequest p = req.productos.get(i);
                    System.out.println("  Producto " + i + ": ID=" + p.idProducto + 
                                     ", Precio=" + p.precio + ", Cantidad=" + p.cantidad + 
                                     ", Descuento=" + p.descuento);
                }
                
                detalles = req.productos.stream()
                    .filter(p -> p.idProducto != null) // Filtrar productos con ID null
                    .map(p -> new FacturaService.DetalleFacturaRequest(
                        p.idProducto, p.precio, p.cantidad, p.descuento != null ? p.descuento : BigDecimal.ZERO))
                    .collect(java.util.stream.Collectors.toList());
                    
                System.out.println("DEBUG - Productos válidos después del filtro: " + detalles.size());
            }
            
            // Actualizar factura completa
            facturaService.actualizarFacturaCompleta(id, req.numero, fechaDate, req.idCliente, 
                req.metodoPago != null ? req.metodoPago : "EFECTIVO", 
                req.estado != null ? req.estado : "PENDIENTE", 
                req.observaciones, detalles);
                
            return ResponseEntity.ok(Map.of("id", id, "mensaje", "Factura actualizada correctamente"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Validación", "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "mensaje", "Error al actualizar la factura: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/detalles")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<?> obtenerDetalles(@PathVariable Long id) {
        try {
            System.out.println("DEBUG - Obteniendo detalles para factura ID: " + id);
            List<Map<String, Object>> detalles = facturaService.obtenerDetalles(id);
            System.out.println("DEBUG - Encontrados " + detalles.size() + " detalles");
            for (int i = 0; i < detalles.size(); i++) {
                Map<String, Object> detalle = detalles.get(i);
                System.out.println("DEBUG - Detalle " + i + ":");
                for (Map.Entry<String, Object> entry : detalle.entrySet()) {
                    System.out.println("  " + entry.getKey() + " = " + entry.getValue());
                }
            }
            return ResponseEntity.ok(detalles);
        } catch (Exception e) {
            System.err.println("ERROR - En obtener detalles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener detalles: " + e.getMessage()));
        }
    }

    // Endpoint temporal para agregar datos de prueba
    @PostMapping("/{id}/agregar-datos-prueba")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> agregarDatosPrueba(@PathVariable Long id) {
        try {
            System.out.println("DEBUG - Agregando datos de prueba a la factura " + id);
            
            // Agregar algunos productos de prueba (IDs 1, 2, 3 con datos básicos)
            facturaService.agregarDetalle(id, 1L, new java.math.BigDecimal("15.50"), 2, java.math.BigDecimal.ZERO);
            facturaService.agregarDetalle(id, 2L, new java.math.BigDecimal("25.00"), 1, new java.math.BigDecimal("2.50"));
            
            return ResponseEntity.ok(Map.of("mensaje", "Datos de prueba agregados correctamente"));
        } catch (Exception e) {
            System.err.println("ERROR - Al agregar datos de prueba: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "mensaje", "Error al agregar datos de prueba: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> anular(@PathVariable Long id) {
        facturaService.anular(id);
        return ResponseEntity.ok().build();
    }
}



