package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Cliente;
import com.ferreteria.sistema.entity.DetalleFactura;
import com.ferreteria.sistema.entity.Factura;
import com.ferreteria.sistema.entity.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
public class FacturaSpDao {
    private final JdbcTemplate jdbcTemplate;
    public FacturaSpDao(DataSource dataSource) { this.jdbcTemplate = new JdbcTemplate(dataSource); }

    private RowMapper<Factura> mapperFactura() {
        return new RowMapper<Factura>() {
            @Override public Factura mapRow(ResultSet rs, int rowNum) throws SQLException {
                Factura f = new Factura();
                f.setIdFactura(rs.getLong("IDFACTURA"));
                f.setNumeroFactura(rs.getString("NUMERO_FACTURA"));
                java.sql.Timestamp ts = rs.getTimestamp("FECHA");
                if (ts != null) {
                    f.setFecha(ts.toLocalDateTime().toLocalDate());
                }
                f.setSubtotal(rs.getBigDecimal("SUBTOTAL"));
                f.setImpuesto(rs.getBigDecimal("IMPUESTO"));
                f.setTotal(rs.getBigDecimal("TOTAL"));
                f.setEstado(Factura.EstadoFactura.valueOf(rs.getString("ESTADO")));
                f.setMetodoPago(Factura.MetodoPago.valueOf(rs.getString("METODO_PAGO")));
                f.setObservaciones(rs.getString("OBSERVACIONES"));
                return f;
            }
        };
    }

    // RowMapper que incluye información del cliente
    private RowMapper<Factura> mapperFacturaConCliente() {
        return new RowMapper<Factura>() {
            @Override public Factura mapRow(ResultSet rs, int rowNum) throws SQLException {
                Factura f = new Factura();
                f.setIdFactura(rs.getLong("IdFactura"));
                f.setNumeroFactura(rs.getString("numero_factura"));
                java.sql.Timestamp ts = rs.getTimestamp("fecha");
                if (ts != null) {
                    f.setFecha(ts.toLocalDateTime().toLocalDate());
                }
                f.setSubtotal(rs.getBigDecimal("subtotal"));
                f.setImpuesto(rs.getBigDecimal("impuesto"));
                f.setTotal(rs.getBigDecimal("total"));
                f.setEstado(Factura.EstadoFactura.valueOf(rs.getString("estado")));
                f.setMetodoPago(Factura.MetodoPago.valueOf(rs.getString("metodo_pago")));
                f.setObservaciones(rs.getString("observaciones"));
                
                // Crear y asignar cliente
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getLong("IdCliente"));
                cliente.setNombreCliente(rs.getString("nombreCliente"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefono(rs.getString("telefono"));
                f.setCliente(cliente);
                
                // Crear y asignar usuario (si existe)
                try {
                    Long idUsuario = rs.getLong("IdUsuario");
                    if (!rs.wasNull() && idUsuario > 0) {
                        Usuario usuario = new Usuario();
                        usuario.setIdUsuario(idUsuario);
                        usuario.setNombreUsuario(rs.getString("nombreUsuario"));
                        usuario.setEmail(rs.getString("emailUsuario"));
                        usuario.setNombre(rs.getString("nombreUsuarioReal"));
                        usuario.setApellidos(rs.getString("apellidos"));
                        f.setUsuario(usuario);
                        
                        String nombreCompleto = usuario.getNombre() != null && usuario.getApellidos() != null 
                            ? usuario.getNombre() + " " + usuario.getApellidos()
                            : usuario.getNombreUsuario();
                        System.out.println("DEBUG - Usuario asignado: " + nombreCompleto + " (" + usuario.getNombreUsuario() + ", ID: " + idUsuario + ")");
                    } else {
                        System.out.println("DEBUG - Sin usuario asignado a la factura");
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG - Error cargando usuario: " + e.getMessage());
                }
                
                // Debug temporal
                System.out.println("DEBUG - Factura ID: " + f.getIdFactura() + 
                                   ", Cliente: " + cliente.getNombreCliente() +
                                   ", Cliente ID: " + cliente.getIdCliente() +
                                   ", Usuario: " + (f.getUsuario() != null ? f.getUsuario().getNombreUsuario() : "NULL"));
                
                return f;
            }
        };
    }

    public List<Factura> listar() {
        try {
            String sql = "SELECT f.IdFactura, f.numero_factura, f.fecha, f.total, f.subtotal, f.impuesto, " +
                        "f.descuento, f.estado, f.metodo_pago, f.observaciones, f.IdCliente, f.IdUsuario, " +
                        "c.nombreCliente, c.email, c.telefono, " +
                        "u.nombreUsuario, u.email as emailUsuario, u.nombre as nombreUsuarioReal, u.apellidos " +
                        "FROM Factura f " +
                        "LEFT JOIN Clientes c ON f.IdCliente = c.IdCliente " +
                        "LEFT JOIN Usuarios u ON f.IdUsuario = u.IdUsuario " +
                        "ORDER BY f.fecha DESC";
            
            return jdbcTemplate.query(sql, mapperFacturaConCliente());
        } catch (Exception e) {
            System.err.println("Error listando facturas: " + e.getMessage());
            // Fallback al método original
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_VENTAS").withFunctionName("FN_LISTAR_FACTURAS")
                    .returningResultSet("RETURN_VALUE", mapperFactura());
            Map<String, Object> out = call.execute(new HashMap<>());
            @SuppressWarnings("unchecked") List<Factura> list = (List<Factura>) out.get("RETURN_VALUE");
            return list != null ? list : Collections.emptyList();
        }
    }

    public Optional<Factura> obtenerPorId(Long id) {
        try {
            String sql = "SELECT f.IdFactura, f.numero_factura, f.fecha, f.total, f.subtotal, f.impuesto, " +
                        "f.descuento, f.estado, f.metodo_pago, f.observaciones, f.IdCliente, f.IdUsuario, " +
                        "c.nombreCliente, c.email, c.telefono, " +
                        "u.nombreUsuario, u.email as emailUsuario, u.nombre as nombreUsuarioReal, u.apellidos " +
                        "FROM Factura f " +
                        "LEFT JOIN Clientes c ON f.IdCliente = c.IdCliente " +
                        "LEFT JOIN Usuarios u ON f.IdUsuario = u.IdUsuario " +
                        "WHERE f.IdFactura = ?";
            
            List<Factura> facturas = jdbcTemplate.query(sql, mapperFacturaConCliente(), id);
            return facturas.isEmpty() ? Optional.empty() : Optional.of(facturas.get(0));
        } catch (Exception e) {
            System.err.println("Error obteniendo factura por ID: " + e.getMessage());
            // Fallback al método original
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_VENTAS").withFunctionName("FN_OBTENER_FACTURA")
                    .returningResultSet("RETURN_VALUE", mapperFactura());
            Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
            @SuppressWarnings("unchecked") List<Factura> list = (List<Factura>) out.get("RETURN_VALUE");
            if (list == null || list.isEmpty()) return Optional.empty();
            return Optional.of(list.get(0));
        }
    }

    public Long crearFacturaBasica(String numero, Date fecha, Long idCliente, String metodoPago, String estado, String observ) {
        // Convertir estado y método de pago a mayúsculas para cumplir con las restricciones de la BD
        String estadoUpper = estado != null ? estado.toUpperCase() : "PENDIENTE";
        String metodoPagoUpper = metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO";
        
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_NUMERO", numero)
                .addValue("P_FECHA", new java.sql.Timestamp(fecha != null ? fecha.getTime() : System.currentTimeMillis()))
                .addValue("P_ID_CLIENTE", idCliente)
                .addValue("P_METODO_PAGO", metodoPagoUpper)
                .addValue("P_ESTADO", estadoUpper)
                .addValue("P_OBSERVACIONES", observ)
                .addValue("P_ID_FACTURA", null);
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_VENTAS").withProcedureName("SP_INSERTAR_FACTURA");
        Map<String, Object> out = call.execute(in);
        Object id = out.get("P_ID_FACTURA");
        if (id instanceof Number) return ((Number) id).longValue();
        return null;
    }

    public void agregarDetalle(Long idFactura, Long idProducto, java.math.BigDecimal precio, Integer cantidad, java.math.BigDecimal desc) {
        // Validaciones de entrada
        if (idFactura == null) {
            throw new IllegalArgumentException("ID de factura no puede ser null");
        }
        if (idProducto == null) {
            throw new IllegalArgumentException("ID de producto no puede ser null");
        }
        
        System.out.println("DEBUG - Agregando detalle - Factura: " + idFactura + 
                          ", Producto: " + idProducto + ", Precio: " + precio + 
                          ", Cantidad: " + cantidad + ", Descuento: " + desc);
        
        try {
            // Validación previa: verificar existencia de registro de stock y disponibilidad
            Integer stockActual = null;
            try {
                stockActual = jdbcTemplate.queryForObject(
                    "SELECT CANTIDAD FROM STOCK WHERE IDPRODUCTO = ?",
                    Integer.class,
                    idProducto
                );
            } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
                // No existe registro de stock para el producto → evitar error ORA-01403 del trigger
                System.out.println("DEBUG - No existe registro de stock para el producto " + idProducto + ". Creando con cantidad 0.");
                jdbcTemplate.update(
                    "INSERT INTO STOCK (CANTIDAD, IDPRODUCTO, UBICACION) VALUES (0, ?, 'ALMACEN PRINCIPAL')",
                    idProducto
                );
                stockActual = 0;
            }

            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad del detalle debe ser mayor a 0");
            }

            if (stockActual != null && stockActual < cantidad) {
                String msg = "Stock insuficiente para el producto (ID=" + idProducto + ") - Disponible: " + stockActual + ", Requerido: " + cantidad + ".";
                System.out.println("DEBUG - " + msg);
                throw new IllegalArgumentException(msg);
            }

            String sql = "INSERT INTO detalleFactura (precioUni, cantidad, descuento_item, IdFactura, IdProducto) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            int result = jdbcTemplate.update(sql, 
                precio != null ? precio : java.math.BigDecimal.ZERO,
                cantidad != null ? cantidad : 0,
                desc != null ? desc : java.math.BigDecimal.ZERO,
                idFactura,
                idProducto
            );
            
            System.out.println("DEBUG - Detalle agregado exitosamente: " + result + " filas afectadas");
        } catch (Exception e) {
            System.err.println("ERROR - Agregando detalle con parámetros: " +
                             "idFactura=" + idFactura + ", idProducto=" + idProducto + 
                             ", precio=" + precio + ", cantidad=" + cantidad + ", descuento=" + desc);
            System.err.println("ERROR - Mensaje: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new RuntimeException("Error al agregar detalle a la factura", e);
        }
    }

    public void actualizarFactura(Long id, String numero, Date fecha, Long idCliente, String metodoPago, String estado, String observ) {
        try {
            String sql = "UPDATE Factura SET " +
                        "numero_factura = ?, " +
                        "fecha = ?, " +
                        "IdCliente = ?, " +
                        "metodo_pago = ?, " +
                        "estado = ?, " +
                        "observaciones = ?, " +
                        "fecha_modificacion = SYSDATE " +
                        "WHERE IdFactura = ?";
            
            // Convertir estado y método de pago a mayúsculas para cumplir con las restricciones de la BD
            String estadoUpper = estado != null ? estado.toUpperCase() : "PENDIENTE";
            String metodoPagoUpper = metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO";
            
            jdbcTemplate.update(sql, 
                numero, 
                new java.sql.Timestamp(fecha != null ? fecha.getTime() : System.currentTimeMillis()),
                idCliente,
                metodoPagoUpper,
                estadoUpper,
                observ,
                id
            );
        } catch (Exception e) {
            System.err.println("Error actualizando factura: " + e.getMessage());
            throw new RuntimeException("Error al actualizar la factura", e);
        }
    }

    public void eliminarDetalles(Long idFactura) {
        try {
            String sql = "DELETE FROM detalleFactura WHERE IdFactura = ?";
            int deletedRows = jdbcTemplate.update(sql, idFactura);
            System.out.println("Eliminados " + deletedRows + " detalles de la factura " + idFactura);
        } catch (Exception e) {
            System.err.println("Error eliminando detalles de factura: " + e.getMessage());
            throw new RuntimeException("Error al eliminar detalles de la factura", e);
        }
    }

    public List<java.util.Map<String, Object>> obtenerDetalles(Long idFactura) {
        try {
            return jdbcTemplate.queryForList(
                "SELECT d.IdDetalle, d.precioUni, d.cantidad, d.descuento_item, " +
                "p.IdProducto, p.nombreProducto, p.descripcion " +
                "FROM detalleFactura d " +
                "INNER JOIN Productos p ON d.IdProducto = p.IdProducto " +
                "WHERE d.IdFactura = ? " +
                "ORDER BY d.IdDetalle",
                idFactura
            );
        } catch (Exception e) {
            System.err.println("Error obteniendo detalles de factura: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public void anular(Long id) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_VENTAS").withProcedureName("SP_ANULAR_FACTURA")
                .execute(new MapSqlParameterSource().addValue("P_ID", id));
    }
}



