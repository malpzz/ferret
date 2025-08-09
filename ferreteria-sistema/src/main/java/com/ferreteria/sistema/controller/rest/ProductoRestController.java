package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Producto;
import com.ferreteria.sistema.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> crear(@RequestBody Producto p) {
        productoService.crear(p);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto p) {
        productoService.actualizar(id, p);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}




