package com.ferreteria.sistema.service;

import com.ferreteria.sistema.entity.Usuario;
import com.ferreteria.sistema.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Servicio personalizado para cargar detalles de usuario en Spring Security
 * 
 * Esta clase implementa UserDetailsService para integrar la autenticación
 * de Spring Security con nuestro modelo de datos de usuarios.
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por su nombre de usuario o email
     * 
     * @param usernameOrEmail nombre de usuario o email
     * @return UserDetails objeto con información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Buscar usuario por nombre de usuario o email
        Usuario usuario = usuarioRepository.findByNombreUsuarioOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con nombre de usuario o email: " + usernameOrEmail));

        // Crear y retornar objeto UserDetails personalizado
        return new CustomUserPrincipal(usuario);
    }

    /**
     * Clase interna que implementa UserDetails
     * Representa los detalles del usuario para Spring Security
     */
    public static class CustomUserPrincipal implements UserDetails {
        private Usuario usuario;

        public CustomUserPrincipal(Usuario usuario) {
            this.usuario = usuario;
        }

        /**
         * Obtiene las autoridades (roles) del usuario
         * @return colección de autoridades
         */
        @Override
        public List<GrantedAuthority> getAuthorities() {
            // Convertir el rol del usuario en una autoridad de Spring Security
            String roleName = "ROLE_" + usuario.getRol().getNombre().toUpperCase();
            return Collections.singletonList(new SimpleGrantedAuthority(roleName));
        }

        /**
         * Obtiene la contraseña del usuario
         * @return contraseña encriptada
         */
        @Override
        public String getPassword() {
            return usuario.getContraseña();
        }

        /**
         * Obtiene el nombre de usuario
         * @return nombre de usuario
         */
        @Override
        public String getUsername() {
            return usuario.getNombreUsuario();
        }

        /**
         * Verifica si la cuenta no ha expirado
         * @return true si la cuenta no ha expirado
         */
        @Override
        public boolean isAccountNonExpired() {
            // Implementar lógica de expiración si es necesario
            // Por ahora, las cuentas no expiran
            return true;
        }

        /**
         * Verifica si la cuenta no está bloqueada
         * @return true si la cuenta no está bloqueada
         */
        @Override
        public boolean isAccountNonLocked() {
            // Bloquear cuenta si hay demasiados intentos fallidos
            return usuario.getIntentosFallidos() < 5;
        }

        /**
         * Verifica si las credenciales no han expirado
         * @return true si las credenciales no han expirado
         */
        @Override
        public boolean isCredentialsNonExpired() {
            // Las credenciales no expiran por defecto
            // Se puede implementar lógica de expiración de contraseñas aquí
            return true;
        }

        /**
         * Verifica si el usuario está habilitado
         * @return true si el usuario está activo
         */
        @Override
        public boolean isEnabled() {
            return usuario.getActivo();
        }

        /**
         * Obtiene el objeto Usuario completo
         * @return usuario
         */
        public Usuario getUsuario() {
            return usuario;
        }

        /**
         * Obtiene el ID del usuario
         * @return ID del usuario
         */
        public Long getId() {
            return usuario.getIdUsuario();
        }

        /**
         * Obtiene el nombre completo del usuario
         * @return nombre completo
         */
        public String getNombreCompleto() {
            return usuario.getNombreCompleto();
        }

        /**
         * Obtiene el email del usuario
         * @return email
         */
        public String getEmail() {
            return usuario.getEmail();
        }

        /**
         * Obtiene el rol del usuario
         * @return nombre del rol
         */
        public String getRol() {
            return usuario.getRol().getNombre();
        }

        /**
         * Verifica si el usuario tiene un rol específico
         * @param rol nombre del rol a verificar
         * @return true si el usuario tiene el rol
         */
        public boolean hasRole(String rol) {
            return usuario.getRol().getNombre().equalsIgnoreCase(rol);
        }

        /**
         * Verifica si el usuario es administrador
         * @return true si es administrador
         */
        public boolean isAdmin() {
            return hasRole("ADMINISTRADOR");
        }

        /**
         * Verifica si el usuario es gerente
         * @return true si es gerente
         */
        public boolean isGerente() {
            return hasRole("GERENTE");
        }

        /**
         * Verifica si el usuario es vendedor
         * @return true si es vendedor
         */
        public boolean isVendedor() {
            return hasRole("VENDEDOR");
        }

        /**
         * Verifica si el usuario es bodeguero
         * @return true si es bodeguero
         */
        public boolean isBodeguero() {
            return hasRole("BODEGUERO");
        }
    }
}

