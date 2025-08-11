package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Rol;
import com.ferreteria.sistema.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolRestController {

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Rol>> listar() {
        return ResponseEntity.ok(rolRepository.findAllByOrderByNombreAsc());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return rolRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> crear(@RequestBody Rol rol) {
        try {
            if (rolRepository.existsByNombre(rol.getNombre())) {
                return ResponseEntity.badRequest().body(error("Error de validación", "Ya existe un rol con ese nombre"));
            }
            rol.setActivo(true);
            Rol creado = rolRepository.save(rol);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Error interno", "No se pudo crear el rol"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Rol datos) {
        return rolRepository.findById(id).map(rol -> {
            if (rolRepository.existsByNombreAndIdRolNot(datos.getNombre(), id)) {
                return ResponseEntity.badRequest().body(error("Error de validación", "Ya existe otro rol con ese nombre"));
            }
            rol.setNombre(datos.getNombre());
            rol.setDescripcion(datos.getDescripcion());
            rol.setActivo(datos.getActivo() != null ? datos.getActivo() : rol.getActivo());
            return ResponseEntity.ok(rolRepository.save(rol));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        return rolRepository.findById(id).map(rol -> {
            Long count = rolRepository.countUsuariosActivosByRol(id);
            if (count != null && count > 0) {
                return ResponseEntity.badRequest().body(error("Error", "No se puede eliminar el rol con usuarios activos asociados"));
            }
            rolRepository.delete(rol);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, String> error(String tipo, String mensaje) {
        Map<String, String> map = new HashMap<>();
        map.put("tipo", tipo);
        map.put("mensaje", mensaje);
        return map;
    }
}


