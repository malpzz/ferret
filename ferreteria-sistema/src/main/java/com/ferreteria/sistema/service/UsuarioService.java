package com.ferreteria.sistema.service;

import com.ferreteria.sistema.entity.Rol;
import com.ferreteria.sistema.entity.Usuario;
import com.ferreteria.sistema.repository.RolRepository;
import com.ferreteria.sistema.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios del sistema
 * 
 * Esta clase contiene la lógica de negocio para crear, actualizar,
 * eliminar y consultar usuarios de la ferretería.
 */
@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios
     * @return lista de usuarios
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene todos los usuarios activos
     * @return lista de usuarios activos
     */
    public List<Usuario> obtenerActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Obtiene un usuario por su nombre de usuario
     * @param nombreUsuario nombre de usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> obtenerPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }

    /**
     * Obtiene un usuario por su email
     * @param email email del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Busca usuarios por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de usuarios que coinciden
     */
    public List<Usuario> buscarPorNombreOApellidos(String texto) {
        return usuarioRepository.buscarPorNombreOApellidos(texto);
    }

    /**
     * Obtiene usuarios por rol
     * @param rolId ID del rol
     * @return lista de usuarios con el rol especificado
     */
    public List<Usuario> obtenerPorRol(Long rolId) {
        return usuarioRepository.findByRolId(rolId);
    }

    /**
     * Crea un nuevo usuario
     * @param usuario datos del usuario a crear
     * @return usuario creado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Usuario crear(Usuario usuario) {
        // Validar que el nombre de usuario no exista
        if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con este nombre de usuario");
        }

        // Validar que el email no exista (si se proporciona)
        if (usuario.getEmail() != null && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }

        // Validar que el rol exista
        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null) {
            throw new IllegalArgumentException("Debe especificar un rol válido");
        }

        Rol rol = rolRepository.findById(usuario.getRol().getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

        if (!rol.getActivo()) {
            throw new IllegalArgumentException("El rol especificado está inactivo");
        }

        // Encriptar la contraseña
        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));

        // Establecer valores por defecto
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaModificacion(LocalDateTime.now());
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente
     * @param id ID del usuario a actualizar
     * @param usuarioActualizado datos actualizados del usuario
     * @return usuario actualizado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el nombre de usuario no exista en otro usuario
        if (!usuario.getNombreUsuario().equals(usuarioActualizado.getNombreUsuario()) &&
            usuarioRepository.existsByNombreUsuarioAndIdUsuarioNot(usuarioActualizado.getNombreUsuario(), id)) {
            throw new IllegalArgumentException("Ya existe otro usuario con este nombre de usuario");
        }

        // Validar que el email no exista en otro usuario (si se proporciona)
        if (usuarioActualizado.getEmail() != null &&
            !usuarioActualizado.getEmail().equals(usuario.getEmail()) &&
            usuarioRepository.existsByEmailAndIdUsuarioNot(usuarioActualizado.getEmail(), id)) {
            throw new IllegalArgumentException("Ya existe otro usuario con este email");
        }

        // Validar que el rol exista
        if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getIdRol() != null) {
            Rol rol = rolRepository.findById(usuarioActualizado.getRol().getIdRol())
                    .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe"));

            if (!rol.getActivo()) {
                throw new IllegalArgumentException("El rol especificado está inactivo");
            }
            usuario.setRol(rol);
        }

        // Actualizar campos
        usuario.setNombreUsuario(usuarioActualizado.getNombreUsuario());
        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setApellidos(usuarioActualizado.getApellidos());
        usuario.setTelefono(usuarioActualizado.getTelefono());

        // Solo actualizar contraseña si se proporciona una nueva
        if (usuarioActualizado.getContraseña() != null && !usuarioActualizado.getContraseña().isEmpty()) {
            usuario.setContraseña(passwordEncoder.encode(usuarioActualizado.getContraseña()));
        }

        usuario.setFechaModificacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambia la contraseña de un usuario
     * @param id ID del usuario
     * @param contraseñaActual contraseña actual
     * @param nuevaContraseña nueva contraseña
     * @throws IllegalArgumentException si la contraseña actual es incorrecta
     */
    public void cambiarContraseña(Long id, String contraseñaActual, String nuevaContraseña) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(contraseñaActual, usuario.getContraseña())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Establecer nueva contraseña
        usuario.setContraseña(passwordEncoder.encode(nuevaContraseña));
        usuario.setFechaModificacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    /**
     * Activa o desactiva un usuario
     * @param id ID del usuario
     * @param activo true para activar, false para desactivar
     * @return usuario actualizado
     */
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(activo);
        usuario.setFechaModificacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario (eliminación física)
     * @param id ID del usuario a eliminar
     * @throws IllegalArgumentException si el usuario no puede ser eliminado
     */
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar si el usuario tiene registros asociados
        if (!usuario.getFacturas().isEmpty() || !usuario.getPedidos().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el usuario porque tiene registros asociados");
        }

        usuarioRepository.delete(usuario);
    }

    /**
     * Incrementa el contador de intentos fallidos de login
     * @param nombreUsuario nombre de usuario
     */
    public void incrementarIntentosFallidos(String nombreUsuario) {
        usuarioRepository.findByNombreUsuario(nombreUsuario)
                .ifPresent(usuario -> {
                    usuario.incrementarIntentosFallidos();
                    usuarioRepository.save(usuario);
                });
    }

    /**
     * Reinicia el contador de intentos fallidos de login
     * @param nombreUsuario nombre de usuario
     */
    public void reiniciarIntentosFallidos(String nombreUsuario) {
        usuarioRepository.findByNombreUsuario(nombreUsuario)
                .ifPresent(usuario -> {
                    usuario.reiniciarIntentosFallidos();
                    usuario.actualizarUltimoAcceso();
                    usuarioRepository.save(usuario);
                });
    }

    /**
     * Desbloquea usuarios que han estado bloqueados por intentos fallidos
     * @param horasDeBloqueo horas que debe durar el bloqueo
     */
    public void desbloquearUsuarios(int horasDeBloqueo) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusHours(horasDeBloqueo);
        List<Usuario> usuariosBloqueados = usuarioRepository.findUsuariosConIntentosExcesivos(5);

        usuariosBloqueados.stream()
                .filter(u -> u.getFechaModificacion().isBefore(fechaLimite))
                .forEach(usuario -> {
                    usuario.reiniciarIntentosFallidos();
                    usuarioRepository.save(usuario);
                });
    }

    /**
     * Verifica si un nombre de usuario está disponible
     * @param nombreUsuario nombre de usuario a verificar
     * @return true si está disponible
     */
    public boolean esNombreUsuarioDisponible(String nombreUsuario) {
        return !usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    /**
     * Verifica si un email está disponible
     * @param email email a verificar
     * @return true si está disponible
     */
    public boolean esEmailDisponible(String email) {
        return !usuarioRepository.existsByEmail(email);
    }

    /**
     * Obtiene estadísticas de usuarios
     * @return objeto con estadísticas
     */
    public EstadisticasUsuarios obtenerEstadisticas() {
        long totalUsuarios = usuarioRepository.count();
        long usuariosActivos = usuarioRepository.findByActivoTrue().size();
        long usuariosInactivos = usuarioRepository.findByActivoFalse().size();
        long administradores = usuarioRepository.countByRolNombreAndActivoTrue("ADMINISTRADOR");
        long vendedores = usuarioRepository.countByRolNombreAndActivoTrue("VENDEDOR");
        long bodegueros = usuarioRepository.countByRolNombreAndActivoTrue("BODEGUERO");

        return new EstadisticasUsuarios(totalUsuarios, usuariosActivos, usuariosInactivos,
                administradores, vendedores, bodegueros);
    }

    /**
     * Clase para estadísticas de usuarios
     */
    public static class EstadisticasUsuarios {
        private long totalUsuarios;
        private long usuariosActivos;
        private long usuariosInactivos;
        private long administradores;
        private long vendedores;
        private long bodegueros;

        public EstadisticasUsuarios(long totalUsuarios, long usuariosActivos, long usuariosInactivos,
                                   long administradores, long vendedores, long bodegueros) {
            this.totalUsuarios = totalUsuarios;
            this.usuariosActivos = usuariosActivos;
            this.usuariosInactivos = usuariosInactivos;
            this.administradores = administradores;
            this.vendedores = vendedores;
            this.bodegueros = bodegueros;
        }

        // Getters
        public long getTotalUsuarios() { return totalUsuarios; }
        public long getUsuariosActivos() { return usuariosActivos; }
        public long getUsuariosInactivos() { return usuariosInactivos; }
        public long getAdministradores() { return administradores; }
        public long getVendedores() { return vendedores; }
        public long getBodegueros() { return bodegueros; }
    }
}

