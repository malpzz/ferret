package com.ferreteria.sistema.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de seguridad para el sistema de ferretería
 * 
 * Esta clase configura la autenticación, autorización y protección
 * de rutas del sistema web.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    // Eliminado: inyección de handlers para evitar ciclo de dependencias.

    /**
     * Configuración del codificador de contraseñas
     * @return BCryptPasswordEncoder para encriptar contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Fuerza 12 para mayor seguridad
    }

    /**
     * Configuración del proveedor de autenticación
     * @return DaoAuthenticationProvider configurado
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // Para mejor manejo de errores
        return authProvider;
    }

    /**
     * Configuración del administrador de autenticación
     * @param config configuración de autenticación
     * @return AuthenticationManager
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configuración principal de la cadena de filtros de seguridad
     * @param http objeto HttpSecurity para configurar
     * @return SecurityFilterChain configurado
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationSuccessHandler customAuthenticationSuccessHandler,
                                           AuthenticationFailureHandler customAuthenticationFailureHandler) throws Exception {
        http
            // Configuración de autorización de solicitudes
            .authorizeHttpRequests(authz -> authz
                // Recursos públicos - sin autenticación requerida
                .requestMatchers(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/favicon.ico",
                    "/error",
                    "/login",
                    "/register",
                    "/api/public/**"
                ).permitAll()
                
                // Endpoints de administración - solo para administradores
                .requestMatchers("/admin/**", "/usuarios/**", "/roles/**").hasRole("ADMINISTRADOR")
                
                // Endpoints de gestión - para gerentes y administradores
                .requestMatchers("/reportes/**", "/estadisticas/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                
                // Endpoints de ventas - para vendedores, gerentes y administradores
                .requestMatchers("/facturas/**", "/clientes/**").hasAnyRole("ADMINISTRADOR", "GERENTE", "VENDEDOR")
                
                // Endpoints de inventario - para bodegueros, gerentes y administradores
                .requestMatchers("/productos/**", "/stock/**", "/pedidos/**", "/proveedores/**")
                    .hasAnyRole("ADMINISTRADOR", "GERENTE", "BODEGUERO")
                
                // Endpoints de empleados - para gerentes y administradores
                .requestMatchers("/empleados/**", "/horarios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                
                // APIs REST - requieren autenticación
                .requestMatchers("/api/**").authenticated()
                
                // Dashboard y otras rutas - requieren autenticación
                .requestMatchers("/", "/dashboard", "/perfil/**").authenticated()
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            
            // Configuración del logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", "FERRETERIA_SESSION")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Configuración de sesiones
            .sessionManagement(session -> session
                .maximumSessions(3) // Máximo 3 sesiones por usuario
                .maxSessionsPreventsLogin(false) // Permite nuevas sesiones expirando las antiguas
                .expiredUrl("/login?expired")
            )
            
            // Configuración de recordar usuario
            .rememberMe(remember -> remember
                .key("ferreteria-remember-me-key")
                .tokenValiditySeconds(86400 * 7) // 7 días
                .userDetailsService(userDetailsService)
            )
            
            // Configuración de headers de seguridad
            .headers(headers -> {
                headers.frameOptions(frame -> frame.deny()); // Previene clickjacking
                headers.contentTypeOptions(Customizer.withDefaults());
                headers.httpStrictTransportSecurity(hstsConfig -> hstsConfig
                        .maxAgeInSeconds(31536000) // 1 año
                        .includeSubDomains(true)
                );
            })
            
            // Configuración CSRF (para formularios web)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Deshabilitar CSRF para APIs REST
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // Configuración del proveedor de autenticación
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Manejador de éxito de autenticación personalizado
     * @return CustomAuthenticationSuccessHandler
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    /**
     * Manejador de fallo de autenticación personalizado
     * @return CustomAuthenticationFailureHandler
     */
    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
}

/**
 * Manejador personalizado para autenticación exitosa
 */
class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.core.Authentication authentication) 
            throws java.io.IOException, jakarta.servlet.ServletException {
        
        // Registrar último acceso del usuario
        // Esto se implementaría en el servicio de usuario
        
        // Redireccionar al dashboard principal para todos los roles
        // Los permisos específicos se controlan por Spring Security
        String redirectURL = request.getContextPath() + "/dashboard";
        
        response.sendRedirect(redirectURL);
    }
}

/**
 * Manejador personalizado para fallo de autenticación
 */
class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception) 
            throws java.io.IOException, jakarta.servlet.ServletException {
        
        // Incrementar contador de intentos fallidos
        // Esto se implementaría en el servicio de usuario
        
        String errorMessage = "Credenciales inválidas";
        
        if (exception.getMessage().contains("locked")) {
            errorMessage = "Cuenta bloqueada por múltiples intentos fallidos";
        } else if (exception.getMessage().contains("disabled")) {
            errorMessage = "Cuenta deshabilitada";
        } else if (exception.getMessage().contains("expired")) {
            errorMessage = "Cuenta expirada";
        }
        
        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect(request.getContextPath() + "/login?error");
    }
}

