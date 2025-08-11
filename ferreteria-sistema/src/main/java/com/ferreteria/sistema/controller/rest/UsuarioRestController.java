package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Rol;
import com.ferreteria.sistema.entity.Usuario;
import com.ferreteria.sistema.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Usuario>> listar() {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        System.out.println("DEBUG - Enviando " + usuarios.size() + " usuarios al frontend");
        for (Usuario u : usuarios) {
            System.out.println("Usuario: " + u.getIdUsuario() + " - " + u.getNombreUsuario() + 
                             " (" + u.getNombreCompleto().trim() + ")");
            System.out.println("  Rol info: " + (u.getRolInfo() != null ? 
                u.getRolInfo().getNombre() + " (ID: " + u.getRolInfo().getIdRol() + ")" : "NULL"));
        }
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public ResponseEntity<?> crear(@RequestBody CrearUsuarioRequest req) {
        try {
            Usuario u = new Usuario();
            u.setNombreUsuario(req.nombreUsuario);
            u.setContraseña(req.contraseña);
            u.setEmail(req.email);
            u.setNombre(req.nombre);
            u.setApellidos(req.apellidos);
            u.setTelefono(req.telefono);
            Rol rol = new Rol();
            rol.setIdRol(req.idRol);
            u.setRol(rol);
            Usuario creado = usuarioService.crear(u);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("Error de validación", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Error interno", "No se pudo crear el usuario"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest req) {
        try {
            Usuario u = new Usuario();
            u.setNombreUsuario(req.nombreUsuario);
            u.setEmail(req.email);
            u.setNombre(req.nombre);
            u.setApellidos(req.apellidos);
            u.setTelefono(req.telefono);
            if (req.contraseña != null && !req.contraseña.isEmpty()) {
                u.setContraseña(req.contraseña);
            }
            if (req.idRol != null) {
                Rol rol = new Rol();
                rol.setIdRol(req.idRol);
                u.setRol(rol);
            }
            Usuario actualizado = usuarioService.actualizar(id, u);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("Error de validación", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Error interno", "No se pudo actualizar el usuario"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("Error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("Error", e.getMessage()));
        }
    }

    private Map<String, String> error(String tipo, String mensaje) {
        Map<String, String> map = new HashMap<>();
        map.put("tipo", tipo);
        map.put("mensaje", mensaje);
        return map;
    }

    public static class CrearUsuarioRequest {
        public String nombreUsuario;
        public String contraseña;
        public String email;
        public String nombre;
        public String apellidos;
        public String telefono;
        public Long idRol;
    }

    public static class ActualizarUsuarioRequest {
        public String nombreUsuario;
        public String contraseña;
        public String email;
        public String nombre;
        public String apellidos;
        public String telefono;
        public Long idRol;
    }
}


