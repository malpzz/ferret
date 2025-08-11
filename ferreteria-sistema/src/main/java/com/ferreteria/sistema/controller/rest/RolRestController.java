package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Rol;
import com.ferreteria.sistema.entity.Usuario;
import com.ferreteria.sistema.repository.RolRepository;
import com.ferreteria.sistema.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolRestController {

    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<RolWithInfo>> listar() {
        List<Rol> roles = rolRepository.findAllByOrderByNombreAsc();
        List<RolWithInfo> rolesWithInfo = roles.stream().map(rol -> {
            Long usuariosCount = rolRepository.countUsuariosActivosByRol(rol.getIdRol());
            return new RolWithInfo(rol, usuariosCount != null ? usuariosCount : 0L);
        }).collect(Collectors.toList());
        
        System.out.println("DEBUG - Enviando " + rolesWithInfo.size() + " roles con información de usuarios");
        for (RolWithInfo r : rolesWithInfo) {
            System.out.println("Rol: " + r.getNombre() + " - Usuarios: " + r.getUsuariosCount());
        }
        
        return ResponseEntity.ok(rolesWithInfo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        Optional<Rol> rolOpt = rolRepository.findById(id);
        if (rolOpt.isPresent()) {
            Rol rol = rolOpt.get();
            Long usuariosCount = rolRepository.countUsuariosActivosByRol(rol.getIdRol());
            RolWithInfo rolWithInfo = new RolWithInfo(rol, usuariosCount != null ? usuariosCount : 0L);
            return ResponseEntity.ok(rolWithInfo);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> crear(@RequestBody RolSimple rolRequest) {
        try {
            if (rolRepository.existsByNombre(rolRequest.getNombre())) {
                return ResponseEntity.badRequest().body(error("Error de validación", "Ya existe un rol con ese nombre"));
            }
            
            Rol rol = new Rol();
            rol.setNombre(rolRequest.getNombre());
            rol.setDescripcion(rolRequest.getDescripcion());
            rol.setActivo(true);
            
            Rol creado = rolRepository.save(rol);
            Long usuariosCount = rolRepository.countUsuariosActivosByRol(creado.getIdRol());
            RolWithInfo resultado = new RolWithInfo(creado, usuariosCount != null ? usuariosCount : 0L);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Error interno", "No se pudo crear el rol"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody RolSimple rolRequest) {
        return rolRepository.findById(id).map(rol -> {
            if (rolRepository.existsByNombreAndIdRolNot(rolRequest.getNombre(), id)) {
                return ResponseEntity.badRequest().body(error("Error de validación", "Ya existe otro rol con ese nombre"));
            }
            rol.setNombre(rolRequest.getNombre());
            rol.setDescripcion(rolRequest.getDescripcion());
            
            Rol actualizado = rolRepository.save(rol);
            Long usuariosCount = rolRepository.countUsuariosActivosByRol(actualizado.getIdRol());
            RolWithInfo resultado = new RolWithInfo(actualizado, usuariosCount != null ? usuariosCount : 0L);
            
            return ResponseEntity.ok(resultado);
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

    @GetMapping("/{id}/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioInfo>> obtenerUsuariosPorRol(@PathVariable Long id) {
        List<Usuario> usuarios = usuarioRepository.findByRolIdAndActivoTrue(id);
        List<UsuarioInfo> usuariosInfo = usuarios.stream().map(usuario -> 
            new UsuarioInfo(
                usuario.getIdUsuario(),
                usuario.getNombreUsuario(),
                usuario.getNombreCompleto().trim(),
                usuario.getEmail(),
                usuario.getActivo()
            )
        ).collect(Collectors.toList());
        
        System.out.println("DEBUG - Enviando " + usuariosInfo.size() + " usuarios para rol " + id);
        return ResponseEntity.ok(usuariosInfo);
    }

    private Map<String, String> error(String tipo, String mensaje) {
        Map<String, String> map = new HashMap<>();
        map.put("tipo", tipo);
        map.put("mensaje", mensaje);
        return map;
    }

    // DTOs
    public static class RolSimple {
        private String nombre;
        private String descripcion;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    public static class RolWithInfo {
        private Long idRol;
        private String nombre;
        private String descripcion;
        private Boolean activo;
        private Long usuariosCount;

        public RolWithInfo(Rol rol, Long usuariosCount) {
            this.idRol = rol.getIdRol();
            this.nombre = rol.getNombre();
            this.descripcion = rol.getDescripcion();
            this.activo = rol.getActivo();
            this.usuariosCount = usuariosCount;
        }

        // Getters
        public Long getIdRol() { return idRol; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public Boolean getActivo() { return activo; }
        public Long getUsuariosCount() { return usuariosCount; }
    }

    public static class UsuarioInfo {
        private Long idUsuario;
        private String nombreUsuario;
        private String nombreCompleto;
        private String email;
        private Boolean activo;

        public UsuarioInfo(Long idUsuario, String nombreUsuario, String nombreCompleto, String email, Boolean activo) {
            this.idUsuario = idUsuario;
            this.nombreUsuario = nombreUsuario;
            this.nombreCompleto = nombreCompleto;
            this.email = email;
            this.activo = activo;
        }

        // Getters
        public Long getIdUsuario() { return idUsuario; }
        public String getNombreUsuario() { return nombreUsuario; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getEmail() { return email; }
        public Boolean getActivo() { return activo; }
    }
}


