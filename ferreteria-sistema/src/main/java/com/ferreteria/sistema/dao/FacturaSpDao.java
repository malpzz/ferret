package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.DetalleFactura;
import com.ferreteria.sistema.entity.Factura;
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

    public List<Factura> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_VENTAS").withFunctionName("FN_LISTAR_FACTURAS")
                .returningResultSet("RETURN_VALUE", mapperFactura());
        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked") List<Factura> list = (List<Factura>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Optional<Factura> obtenerPorId(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_VENTAS").withFunctionName("FN_OBTENER_FACTURA")
                .returningResultSet("RETURN_VALUE", mapperFactura());
        Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
        @SuppressWarnings("unchecked") List<Factura> list = (List<Factura>) out.get("RETURN_VALUE");
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public Long crearFacturaBasica(String numero, Date fecha, Long idCliente, String metodoPago, String estado, String observ) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_NUMERO", numero)
                .addValue("P_FECHA", new java.sql.Timestamp(fecha != null ? fecha.getTime() : System.currentTimeMillis()))
                .addValue("P_ID_CLIENTE", idCliente)
                .addValue("P_METODO_PAGO", metodoPago)
                .addValue("P_ESTADO", estado)
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
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_VENTAS").withProcedureName("SP_AGREGAR_DETALLE_FACTURA")
                .execute(new MapSqlParameterSource()
                        .addValue("P_ID_FACTURA", idFactura)
                        .addValue("P_ID_PRODUCTO", idProducto)
                        .addValue("P_PRECIO", precio)
                        .addValue("P_CANTIDAD", cantidad)
                        .addValue("P_DESCUENTO", desc)
                );
    }

    public void anular(Long id) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_VENTAS").withProcedureName("SP_ANULAR_FACTURA")
                .execute(new MapSqlParameterSource().addValue("P_ID", id));
    }
}



