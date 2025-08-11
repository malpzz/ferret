package com.ferreteria.sistema.controller.web;

import com.ferreteria.sistema.entity.Rol;
import com.ferreteria.sistema.entity.Usuario;
import com.ferreteria.sistema.repository.RolRepository;
import com.ferreteria.sistema.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class AuthController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    public AuthController(UsuarioService usuarioService, RolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String nombreUsuario,
                             @RequestParam(required = false) String email,
                             @RequestParam("password") String password,
                             @RequestParam("password2") String password2,
                             Model model) {
        if (!password.equals(password2)) {
            model.addAttribute("mensaje", "Las contraseñas no coinciden");
            return "auth/register";
        }

        Usuario u = new Usuario();
        u.setNombreUsuario(nombreUsuario);
        u.setEmail(email);
        u.setContraseña(password);
        // Rol por defecto: VENDEDOR si existe, si no ADMINISTRADOR=1
        Rol rol = rolRepository.findByNombre("VENDEDOR").orElseGet(() -> {
            Rol r = new Rol();
            r.setIdRol(1L);
            return r;
        });
        u.setRol(rol);

        try {
            usuarioService.crear(u);
            model.addAttribute("mensaje", "Usuario creado. Ya puedes iniciar sesión.");
            return "auth/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("mensaje", ex.getMessage());
            return "auth/register";
        }
    }
    

}



