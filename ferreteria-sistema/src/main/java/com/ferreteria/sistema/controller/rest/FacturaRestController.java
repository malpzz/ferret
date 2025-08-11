package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Factura;
import com.ferreteria.sistema.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaRestController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<List<Factura>> listar() { return ResponseEntity.ok(facturaService.obtenerTodos()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return facturaService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CrearFacturaRequest {
        public String numero;
        public String fecha; // Cambiar a String para recibir formato "YYYY-MM-DD"
        public Long idCliente;
        public String metodoPago;
        public String estado;
        public String observaciones;
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
            
            Long id = facturaService.crearBasica(req.numero, fechaDate, req.idCliente, req.metodoPago, req.estado, req.observaciones);
            return ResponseEntity.ok(Map.of("id", id, "mensaje", "Factura creada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error de validación", "mensaje", e.getMessage()));
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
        
        try {
            // Validar campos obligatorios (igual que crear)
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
            
            // TODO: Implementar actualización en el servicio
            // Por ahora devolver éxito simulado
            return ResponseEntity.ok(Map.of("id", id, "mensaje", "Factura actualizada correctamente (simulado)"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error de validación", "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "mensaje", "Error al actualizar la factura: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> anular(@PathVariable Long id) {
        facturaService.anular(id);
        return ResponseEntity.ok().build();
    }
}



