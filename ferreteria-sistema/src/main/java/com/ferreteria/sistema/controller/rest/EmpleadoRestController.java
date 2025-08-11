package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Empleado;
import com.ferreteria.sistema.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoRestController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<List<Empleado>> listar() { return ResponseEntity.ok(empleadoService.obtenerTodos()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return empleadoService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> crear(@RequestBody Empleado e) {
        try {
            empleadoService.crear(e);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al crear empleado", "mensaje", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Empleado e) {
        try {
            empleadoService.actualizar(id, e);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound()
                    .build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al actualizar empleado", "mensaje", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            empleadoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound()
                    .build();
        } catch (RuntimeException ex) {
            // Verificar si es un error de constraint (horarios asociados)
            String mensaje = ex.getMessage();
            if (mensaje != null && (mensaje.contains("horarios asociados") || 
                                   mensaje.contains("tiene horarios"))) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflicto", "mensaje", "No se puede eliminar el empleado porque tiene horarios asociados"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar empleado", "mensaje", ex.getMessage()));
        }
    }
}




