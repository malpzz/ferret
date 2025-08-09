package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Proveedor;
import com.ferreteria.sistema.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorRestController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    public ResponseEntity<List<Proveedor>> listar() {
        return ResponseEntity.ok(proveedorService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO','VENDEDOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return proveedorService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> crear(@RequestBody Proveedor p) {
        proveedorService.crear(p);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Proveedor p) {
        proveedorService.actualizar(id, p);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','BODEGUERO')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}




