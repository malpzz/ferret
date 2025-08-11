package com.ferreteria.sistema.controller.rest;

import com.ferreteria.sistema.entity.Cliente;
import com.ferreteria.sistema.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de clientes
 * 
 * Proporciona endpoints para operaciones CRUD de clientes
 * a través de una API REST.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteRestController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Obtiene todos los clientes
     * @return lista de clientes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Cliente>> obtenerTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Obtiene todos los clientes activos
     * @return lista de clientes activos
     */
    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Cliente>> obtenerActivos() {
        List<Cliente> clientes = clienteService.obtenerActivos();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Obtiene un cliente por su ID
     * @param id ID del cliente
     * @return cliente encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.obtenerPorId(id);
        return cliente.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca clientes por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de clientes que coinciden
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Cliente>> buscar(@RequestParam String texto) {
        List<Cliente> clientes = clienteService.buscarPorNombreOApellidos(texto);
        return ResponseEntity.ok(clientes);
    }

    /**
     * Obtiene clientes por tipo
     * @param tipo tipo de cliente (REGULAR, MAYORISTA, VIP)
     * @return lista de clientes del tipo especificado
     */
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Cliente>> obtenerPorTipo(@PathVariable String tipo) {
        try {
            Cliente.TipoCliente tipoCliente = Cliente.TipoCliente.valueOf(tipo.toUpperCase());
            List<Cliente> clientes = clienteService.obtenerPorTipo(tipoCliente);
            return ResponseEntity.ok(clientes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Crea un nuevo cliente
     * @param cliente datos del cliente a crear
     * @return cliente creado con código 201
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<?> crear(@Valid @RequestBody Cliente cliente) {
        try {
            Cliente clienteCreado = clienteService.crear(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno", "Error al crear el cliente"));
        }
    }

    /**
     * Actualiza un cliente existente
     * @param id ID del cliente a actualizar
     * @param cliente datos actualizados del cliente
     * @return cliente actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        try {
            Cliente clienteActualizado = clienteService.actualizar(id, cliente);
            return ResponseEntity.ok(clienteActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno", "Error al actualizar el cliente"));
        }
    }

    /**
     * Cambia el estado de un cliente (activo/inactivo)
     * @param id ID del cliente
     * @param activo nuevo estado del cliente
     * @return cliente actualizado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            Cliente cliente = clienteService.cambiarEstado(id, activo);
            return ResponseEntity.ok(cliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error", e.getMessage()));
        }
    }

    /**
     * Elimina un cliente
     * @param id ID del cliente a eliminar
     * @return respuesta sin contenido (204) o error
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            clienteService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error", e.getMessage()));
        } catch (DataAccessException e) {
            // Verificar si es un error de constraint (facturas asociadas)
            String mensaje = e.getMessage();
            if (mensaje != null && (mensaje.contains("facturas asociadas") || 
                                   mensaje.contains("ORA-20012") ||
                                   mensaje.contains("tiene facturas"))) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Conflicto", "No se puede eliminar el cliente porque tiene facturas asociadas"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error de base de datos", mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno", "Error al eliminar el cliente"));
        }
    }

    /**
     * Verifica si un email está disponible
     * @param email email a verificar
     * @return true si está disponible
     */
    @GetMapping("/verificar-email")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Boolean> verificarEmail(@RequestParam String email) {
        boolean disponible = clienteService.esEmailDisponible(email);
        return ResponseEntity.ok(disponible);
    }

    /**
     * Verifica si una cédula está disponible
     * @param cedula cédula a verificar
     * @return true si está disponible
     */
    @GetMapping("/verificar-cedula")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Boolean> verificarCedula(@RequestParam String cedula) {
        boolean disponible = clienteService.esCedulaDisponible(cedula);
        return ResponseEntity.ok(disponible);
    }

    /**
     * Obtiene estadísticas de clientes
     * @return estadísticas de clientes
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            // Aquí se implementarían las estadísticas específicas
            // Por ahora retornamos un objeto simple
            return ResponseEntity.ok(new EstadisticasResponse(
                clienteService.contarTodos(),
                clienteService.contarActivos(),
                clienteService.contarPorTipo(Cliente.TipoCliente.VIP),
                clienteService.contarPorTipo(Cliente.TipoCliente.MAYORISTA)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno", "Error al obtener estadísticas"));
        }
    }

    /**
     * Clase para respuestas de error
     */
    public static class ErrorResponse {
        private String tipo;
        private String mensaje;

        public ErrorResponse(String tipo, String mensaje) {
            this.tipo = tipo;
            this.mensaje = mensaje;
        }

        public String getTipo() { return tipo; }
        public String getMensaje() { return mensaje; }
    }

    /**
     * Clase para respuestas de estadísticas
     */
    public static class EstadisticasResponse {
        private long totalClientes;
        private long clientesActivos;
        private long clientesVip;
        private long clientesMayoristas;

        public EstadisticasResponse(long totalClientes, long clientesActivos, 
                                   long clientesVip, long clientesMayoristas) {
            this.totalClientes = totalClientes;
            this.clientesActivos = clientesActivos;
            this.clientesVip = clientesVip;
            this.clientesMayoristas = clientesMayoristas;
        }

        public long getTotalClientes() { return totalClientes; }
        public long getClientesActivos() { return clientesActivos; }
        public long getClientesVip() { return clientesVip; }
        public long getClientesMayoristas() { return clientesMayoristas; }
    }
}

