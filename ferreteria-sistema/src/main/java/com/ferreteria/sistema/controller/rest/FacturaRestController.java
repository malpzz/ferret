package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Factura;
import com.ferreteria.sistema.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
        public Date fecha;
        public Long idCliente;
        public String metodoPago;
        public String estado;
        public String observaciones;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','VENDEDOR')")
    public ResponseEntity<?> crear(@RequestBody CrearFacturaRequest req) {
        Long id = facturaService.crearBasica(req.numero, req.fecha, req.idCliente, req.metodoPago, req.estado, req.observaciones);
        return ResponseEntity.ok(id);
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> anular(@PathVariable Long id) {
        facturaService.anular(id);
        return ResponseEntity.ok().build();
    }
}



