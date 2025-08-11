package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.FacturaSpDao;
import com.ferreteria.sistema.entity.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FacturaService {

    @Autowired
    private FacturaSpDao facturaSpDao;

    public List<Factura> obtenerTodos() { return facturaSpDao.listar(); }

    public Optional<Factura> obtenerPorId(Long id) { return facturaSpDao.obtenerPorId(id); }

    public List<java.util.Map<String, Object>> obtenerDetalles(Long idFactura) {
        return facturaSpDao.obtenerDetalles(idFactura);
    }

    public Long crearBasica(String numero, Date fecha, Long idCliente, String metodoPago, String estado, String obs) {
        return facturaSpDao.crearFacturaBasica(numero, fecha, idCliente, metodoPago, estado, obs);
    }

    public void actualizar(Long id, String numero, Date fecha, Long idCliente, String metodoPago, String estado, String obs) {
        facturaSpDao.actualizarFactura(id, numero, fecha, idCliente, metodoPago, estado, obs);
    }

    public void agregarDetalle(Long idFactura, Long idProducto, BigDecimal precio, Integer cantidad, BigDecimal descuento) {
        System.out.println("DEBUG - Service: Agregando detalle - Factura: " + idFactura + ", Producto: " + idProducto);
        facturaSpDao.agregarDetalle(idFactura, idProducto, precio, cantidad, descuento);
    }

    public void eliminarDetalles(Long idFactura) {
        facturaSpDao.eliminarDetalles(idFactura);
    }

    public void anular(Long id) { facturaSpDao.anular(id); }

    // Método completo para crear factura con productos
    @Transactional
    public Long crearFacturaCompleta(String numero, Date fecha, Long idCliente, String metodoPago, String estado, String obs, List<DetalleFacturaRequest> productos) {
        // Crear factura básica
        Long facturaId = facturaSpDao.crearFacturaBasica(numero, fecha, idCliente, metodoPago, estado, obs);
        
        // Agregar productos si existen
        if (productos != null && !productos.isEmpty()) {
            for (DetalleFacturaRequest detalle : productos) {
                facturaSpDao.agregarDetalle(facturaId, detalle.getIdProducto(), detalle.getPrecio(), detalle.getCantidad(), detalle.getDescuento());
            }
        }
        
        return facturaId;
    }

    // Método completo para actualizar factura con productos  
    @Transactional
    public void actualizarFacturaCompleta(Long id, String numero, Date fecha, Long idCliente, String metodoPago, String estado, String obs, List<DetalleFacturaRequest> productos) {
        // Actualizar datos básicos de la factura
        facturaSpDao.actualizarFactura(id, numero, fecha, idCliente, metodoPago, estado, obs);
        
        // Eliminar detalles existentes
        facturaSpDao.eliminarDetalles(id);
        
        // Agregar nuevos productos si existen
        if (productos != null && !productos.isEmpty()) {
            for (DetalleFacturaRequest detalle : productos) {
                facturaSpDao.agregarDetalle(id, detalle.getIdProducto(), detalle.getPrecio(), detalle.getCantidad(), detalle.getDescuento());
            }
        }
    }

    // Clase interna para los datos del detalle
    public static class DetalleFacturaRequest {
        private Long idProducto;
        private BigDecimal precio;
        private Integer cantidad;
        private BigDecimal descuento;

        public DetalleFacturaRequest() {}

        public DetalleFacturaRequest(Long idProducto, BigDecimal precio, Integer cantidad, BigDecimal descuento) {
            this.idProducto = idProducto;
            this.precio = precio;
            this.cantidad = cantidad;
            this.descuento = descuento;
        }

        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
        
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        
        public BigDecimal getDescuento() { return descuento; }
        public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    }
}




