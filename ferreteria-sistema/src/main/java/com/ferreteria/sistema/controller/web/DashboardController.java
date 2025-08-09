package com.ferreteria.sistema.controller.web;

import com.ferreteria.sistema.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web para el dashboard principal del sistema
 * 
 * Este controlador maneja las vistas principales del sistema de ferretería,
 * incluyendo el dashboard, login y páginas de gestión.
 */
@Controller
@RequestMapping("/")
public class DashboardController {

    /**
     * Página principal - redirecciona al dashboard
     * @return redirección al dashboard
     */
    @GetMapping
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * Dashboard principal del sistema
     * @param model modelo para la vista
     * @param authentication información del usuario autenticado
     * @return vista del dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Obtener información del usuario autenticado
        if (authentication != null) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            model.addAttribute("usuario", userPrincipal.getUsuario());
            model.addAttribute("nombreCompleto", userPrincipal.getNombreCompleto());
            model.addAttribute("rol", userPrincipal.getRol());
            model.addAttribute("esAdmin", userPrincipal.isAdmin());
            model.addAttribute("esGerente", userPrincipal.isGerente());
            model.addAttribute("esVendedor", userPrincipal.isVendedor());
            model.addAttribute("esBodeguero", userPrincipal.isBodeguero());
        }

        // Aquí se pueden agregar estadísticas del dashboard
        // Por ejemplo: total de ventas del día, productos con stock bajo, etc.
        
        return "dashboard/index";
    }

    /**
     * Página de login personalizada
     * @param model modelo para la vista
     * @return vista de login
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("titulo", "Iniciar Sesión - Sistema Ferretería");
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("titulo", "Registro - Sistema Ferretería");
        return "auth/register";
    }

    /**
     * Página de gestión de clientes
     * @param model modelo para la vista
     * @return vista de clientes
     */
    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("titulo", "Gestión de Clientes");
        model.addAttribute("seccion", "clientes");
        return "clientes/index";
    }

    /**
     * Página de gestión de productos
     * @param model modelo para la vista
     * @return vista de productos
     */
    @GetMapping("/productos")
    public String productos(Model model) {
        model.addAttribute("titulo", "Gestión de Productos");
        model.addAttribute("seccion", "productos");
        return "productos/index";
    }

    /**
     * Página de gestión de stock
     * @param model modelo para la vista
     * @return vista de stock
     */
    @GetMapping("/stock")
    public String stock(Model model) {
        model.addAttribute("titulo", "Control de Stock");
        model.addAttribute("seccion", "stock");
        return "stock/index";
    }

    /**
     * Página de gestión de facturas
     * @param model modelo para la vista
     * @return vista de facturas
     */
    @GetMapping("/facturas")
    public String facturas(Model model) {
        model.addAttribute("titulo", "Gestión de Facturas");
        model.addAttribute("seccion", "facturas");
        return "facturas/index";
    }

    /**
     * Página de gestión de pedidos
     * @param model modelo para la vista
     * @return vista de pedidos
     */
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("titulo", "Gestión de Pedidos");
        model.addAttribute("seccion", "pedidos");
        return "pedidos/index";
    }

    /**
     * Página de gestión de empleados
     * @param model modelo para la vista
     * @return vista de empleados
     */
    @GetMapping("/empleados")
    public String empleados(Model model) {
        model.addAttribute("titulo", "Gestión de Empleados");
        model.addAttribute("seccion", "empleados");
        return "empleados/index";
    }

    /**
     * Página de gestión de proveedores
     * @param model modelo para la vista
     * @return vista de proveedores
     */
    @GetMapping("/proveedores")
    public String proveedores(Model model) {
        model.addAttribute("titulo", "Gestión de Proveedores");
        model.addAttribute("seccion", "proveedores");
        return "proveedores/index";
    }

    /**
     * Página de gestión de usuarios
     * @param model modelo para la vista
     * @return vista de usuarios
     */
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("titulo", "Gestión de Usuarios");
        model.addAttribute("seccion", "usuarios");
        return "usuarios/index";
    }

    /**
     * Página de gestión de roles
     * @param model modelo para la vista
     * @return vista de roles
     */
    @GetMapping("/roles")
    public String roles(Model model) {
        model.addAttribute("titulo", "Gestión de Roles");
        model.addAttribute("seccion", "roles");
        return "roles/index";
    }

    /**
     * Página de gestión de horarios
     * @param model modelo para la vista
     * @return vista de horarios
     */
    @GetMapping("/horarios")
    public String horarios(Model model) {
        model.addAttribute("titulo", "Gestión de Horarios");
        model.addAttribute("seccion", "horarios");
        return "horarios/index";
    }

    /**
     * Página de reportes
     * @param model modelo para la vista
     * @return vista de reportes
     */
    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("titulo", "Reportes y Estadísticas");
        model.addAttribute("seccion", "reportes");
        return "reportes/index";
    }

    /**
     * Página de perfil de usuario
     * @param model modelo para la vista
     * @param authentication información del usuario autenticado
     * @return vista de perfil
     */
    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        if (authentication != null) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            model.addAttribute("usuario", userPrincipal.getUsuario());
        }
        
        model.addAttribute("titulo", "Mi Perfil");
        model.addAttribute("seccion", "perfil");
        return "perfil/index";
    }

    /**
     * Página de configuración del sistema
     * @param model modelo para la vista
     * @return vista de configuración
     */
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        model.addAttribute("titulo", "Configuración del Sistema");
        model.addAttribute("seccion", "configuracion");
        return "configuracion/index";
    }

    /**
     * Página de error 403 - Acceso denegado
     * @param model modelo para la vista
     * @return vista de error 403
     */
    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("titulo", "Acceso Denegado");
        model.addAttribute("mensaje", "No tienes permisos para acceder a esta sección");
        return "error/403";
    }

    /**
     * Página de error 404 - No encontrado
     * @param model modelo para la vista
     * @return vista de error 404
     */
    @GetMapping("/404")
    public String notFound(Model model) {
        model.addAttribute("titulo", "Página No Encontrada");
        model.addAttribute("mensaje", "La página que buscas no existe");
        return "error/404";
    }

    /**
     * Página de error 500 - Error interno
     * @param model modelo para la vista
     * @return vista de error 500
     */
    @GetMapping("/500")
    public String internalError(Model model) {
        model.addAttribute("titulo", "Error Interno del Servidor");
        model.addAttribute("mensaje", "Ha ocurrido un error inesperado en el sistema");
        return "error/500";
    }
}

