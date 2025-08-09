package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Pedido;
import com.ferreteria.sistema.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoRestController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<List<Pedido>> listar() { return ResponseEntity.ok(pedidoService.obtenerTodos()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CrearPedidoRequest {
        public String numero;
        public Date fecha;
        public Long idProveedor;
        public String estado;
        public Date fechaEntrega;
        public String descripcion;
        public String observaciones;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> crear(@RequestBody CrearPedidoRequest req) {
        Long id = pedidoService.crear(req.numero, req.fecha, req.idProveedor, req.estado, req.fechaEntrega, req.descripcion, req.observaciones);
        return ResponseEntity.ok(id);
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        pedidoService.actualizarEstado(id, estado);
        return ResponseEntity.ok().build();
    }
}




