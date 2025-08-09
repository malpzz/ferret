package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Horario;
import com.ferreteria.sistema.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        Long id = horarioService.crear(h);
        return ResponseEntity.ok(id);
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




