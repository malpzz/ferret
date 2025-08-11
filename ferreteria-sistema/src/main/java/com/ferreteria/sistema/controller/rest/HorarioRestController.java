package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Horario;
import com.ferreteria.sistema.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "*")
public class HorarioRestController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<List<Horario>> listar() { return ResponseEntity.ok(horarioService.obtenerTodos()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return horarioService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<List<Horario>> listarPorEmpleado(@PathVariable Long idEmpleado) {
        return ResponseEntity.ok(horarioService.listarPorEmpleado(idEmpleado));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> crear(@RequestBody Horario h) {
        try {
            System.out.println("DEBUG - Recibido request para crear horario:");
            System.out.println("DEBUG - JSON recibido - ID Empleado: " + h.getIdEmpleado());
            System.out.println("DEBUG - JSON recibido - Fecha: " + h.getFecha());
            System.out.println("DEBUG - JSON recibido - Hora Entrada: " + h.getHoraEntrada());
            System.out.println("DEBUG - JSON recibido - Hora Salida: " + h.getHoraSalida());
            System.out.println("DEBUG - JSON recibido - Observaciones: " + h.getObservaciones());
            
            Long id = horarioService.crear(h);
            System.out.println("DEBUG - Horario creado con ID: " + id);
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            System.out.println("ERROR en controlador crear horario: " + e.getMessage());
            e.printStackTrace();
            
            // Manejar diferentes tipos de errores con mensajes específicos
            if (e.getMessage() != null && e.getMessage().contains("unique constraint")) {
                if (e.getMessage().contains("UK_HORARIO_EMPLEADO_FECHA")) {
                    return ResponseEntity.status(409).body(
                        Map.of("mensaje", "Ya existe un horario para este empleado en la fecha especificada")
                    );
                }
                return ResponseEntity.status(409).body(
                    Map.of("mensaje", "Error de duplicación: " + e.getMessage())
                );
            }
            
            return ResponseEntity.status(500).body(
                Map.of("mensaje", "Error creando horario: " + e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Horario h) {
        horarioService.actualizar(id, h);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}




