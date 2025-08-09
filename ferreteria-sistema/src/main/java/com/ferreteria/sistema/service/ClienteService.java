package com.ferreteria.sistema.service;

import com.ferreteria.sistema.entity.Cliente;
import com.ferreteria.sistema.repository.ClienteRepository;
import com.ferreteria.sistema.dao.ClienteSpDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de clientes de la ferretería
 * 
 * Esta clase contiene la lógica de negocio para crear, actualizar,
 * eliminar y consultar clientes.
 */
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteSpDao clienteSpDao;

    /**
     * Obtiene todos los clientes
     * @return lista de clientes
     */
    public List<Cliente> obtenerTodos() {
        return clienteSpDao.listar();
    }

    /**
     * Obtiene todos los clientes activos
     * @return lista de clientes activos
     */
    public List<Cliente> obtenerActivos() {
        return clienteRepository.findByActivoTrue();
    }

    /**
     * Obtiene un cliente por su ID
     * @param id ID del cliente
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteSpDao.obtenerPorId(id);
    }

    /**
     * Obtiene un cliente por su email
     * @param email email del cliente
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    /**
     * Obtiene un cliente por su cédula
     * @param cedula cédula del cliente
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> obtenerPorCedula(String cedula) {
        return clienteRepository.findByCedula(cedula);
    }

    /**
     * Busca clientes por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de clientes que coinciden
     */
    public List<Cliente> buscarPorNombreOApellidos(String texto) {
        return clienteRepository.buscarPorNombreOApellidos(texto);
    }

    /**
     * Busca clientes activos por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de clientes activos que coinciden
     */
    public List<Cliente> buscarActivosPorNombreOApellidos(String texto) {
        return clienteRepository.buscarActivosPorNombreOApellidos(texto);
    }

    /**
     * Obtiene clientes por tipo
     * @param tipoCliente tipo de cliente
     * @return lista de clientes del tipo especificado
     */
    public List<Cliente> obtenerPorTipo(Cliente.TipoCliente tipoCliente) {
        return clienteRepository.findByTipoClienteAndActivoTrue(tipoCliente);
    }

    /**
     * Crea un nuevo cliente
     * @param cliente datos del cliente a crear
     * @return cliente creado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Cliente crear(Cliente cliente) {
        // Validar que el email no exista (si se proporciona)
        if (cliente.getEmail() != null && clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con este email");
        }

        // Validar que la cédula no exista (si se proporciona)
        if (cliente.getCedula() != null && clienteRepository.existsByCedula(cliente.getCedula())) {
            throw new IllegalArgumentException("Ya existe un cliente con esta cédula");
        }

        // Validar campos obligatorios
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }

        if (cliente.getApellidos() == null || cliente.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }

        if (cliente.getDireccion() == null || cliente.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }

        if (cliente.getTelefono() == null || cliente.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }

        // Establecer valores por defecto
        cliente.setActivo(true);
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setFechaModificacion(LocalDateTime.now());

        if (cliente.getTipoCliente() == null) {
            cliente.setTipoCliente(Cliente.TipoCliente.REGULAR);
        }

        // Inserta vía SP; luego consulta por cédula/email o retorna básico
        clienteSpDao.insertar(cliente);
        // Intentar obtener por email si está disponible para devolver entidad completa
        if (cliente.getEmail() != null) {
            return clienteRepository.findByEmail(cliente.getEmail()).orElse(cliente);
        }
        return cliente;
    }

    /**
     * Actualiza un cliente existente
     * @param id ID del cliente a actualizar
     * @param clienteActualizado datos actualizados del cliente
     * @return cliente actualizado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Validar que el email no exista en otro cliente (si se proporciona)
        if (clienteActualizado.getEmail() != null &&
            !clienteActualizado.getEmail().equals(cliente.getEmail()) &&
            clienteRepository.existsByEmailAndIdClienteNot(clienteActualizado.getEmail(), id)) {
            throw new IllegalArgumentException("Ya existe otro cliente con este email");
        }

        // Validar que la cédula no exista en otro cliente (si se proporciona)
        if (clienteActualizado.getCedula() != null &&
            !clienteActualizado.getCedula().equals(cliente.getCedula()) &&
            clienteRepository.existsByCedulaAndIdClienteNot(clienteActualizado.getCedula(), id)) {
            throw new IllegalArgumentException("Ya existe otro cliente con esta cédula");
        }

        // Actualizar campos
        cliente.setNombreCliente(clienteActualizado.getNombreCliente());
        cliente.setApellidos(clienteActualizado.getApellidos());
        cliente.setDireccion(clienteActualizado.getDireccion());
        cliente.setTelefono(clienteActualizado.getTelefono());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setCedula(clienteActualizado.getCedula());

        if (clienteActualizado.getTipoCliente() != null) {
            cliente.setTipoCliente(clienteActualizado.getTipoCliente());
        }

        if (clienteActualizado.getLimiteCredito() != null) {
            cliente.setLimiteCredito(clienteActualizado.getLimiteCredito());
        }

        cliente.setFechaModificacion(LocalDateTime.now());

        clienteSpDao.actualizar(id, cliente);
        return clienteRepository.findById(id).orElse(cliente);
    }

    /**
     * Activa o desactiva un cliente
     * @param id ID del cliente
     * @param activo true para activar, false para desactivar
     * @return cliente actualizado
     */
    public Cliente cambiarEstado(Long id, boolean activo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        cliente.setActivo(activo);
        cliente.setFechaModificacion(LocalDateTime.now());

        return clienteRepository.save(cliente);
    }

    /**
     * Elimina un cliente (eliminación física)
     * @param id ID del cliente a eliminar
     * @throws IllegalArgumentException si el cliente no puede ser eliminado
     */
    public void eliminar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Verificar si el cliente tiene facturas asociadas
        if (!cliente.getFacturas().isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el cliente porque tiene facturas asociadas");
        }

        clienteSpDao.eliminar(id);
    }

    /**
     * Verifica si un email está disponible
     * @param email email a verificar
     * @return true si está disponible
     */
    public boolean esEmailDisponible(String email) {
        return !clienteRepository.existsByEmail(email);
    }

    /**
     * Verifica si una cédula está disponible
     * @param cedula cédula a verificar
     * @return true si está disponible
     */
    public boolean esCedulaDisponible(String cedula) {
        return !clienteRepository.existsByCedula(cedula);
    }

    /**
     * Cuenta el total de clientes
     * @return total de clientes
     */
    public long contarTodos() {
        return clienteRepository.count();
    }

    /**
     * Cuenta clientes activos
     * @return cantidad de clientes activos
     */
    public long contarActivos() {
        return clienteRepository.findByActivoTrue().size();
    }

    /**
     * Cuenta clientes por tipo
     * @param tipoCliente tipo de cliente
     * @return cantidad de clientes del tipo especificado
     */
    public long contarPorTipo(Cliente.TipoCliente tipoCliente) {
        return clienteRepository.countByTipoClienteAndActivoTrue(tipoCliente);
    }

    /**
     * Busca clientes con emails que coincidan con un patrón
     * @param patron patrón de expresión regular
     * @return lista de clientes que coinciden
     */
    public List<Cliente> buscarPorPatronEmail(String patron) {
        return clienteRepository.findByEmailMatchingPattern(patron);
    }

    /**
     * Busca clientes con teléfonos que coincidan con un patrón
     * @param patron patrón de expresión regular
     * @return lista de clientes que coinciden
     */
    public List<Cliente> buscarPorPatronTelefono(String patron) {
        return clienteRepository.findByTelefonoMatchingPattern(patron);
    }

    /**
     * Obtiene clientes nuevos registrados en el último mes
     * @return lista de clientes nuevos
     */
    public List<Cliente> obtenerClientesNuevos() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusMonths(1);
        return clienteRepository.findClientesNuevos(fechaLimite);
    }

    /**
     * Obtiene clientes VIP con mayor volumen de compras
     * @param limite monto mínimo de compras
     * @return lista de clientes VIP
     */
    public List<Cliente> obtenerClientesVIPConMayorCompras(java.math.BigDecimal limite) {
        return clienteRepository.findClientesVIPConMayorCompras(limite);
    }

    /**
     * Obtiene estadísticas de clientes por tipo
     * @return lista con estadísticas por tipo
     */
    public List<Object[]> obtenerEstadisticasPorTipo() {
        return clienteRepository.getEstadisticasPorTipo();
    }
}
