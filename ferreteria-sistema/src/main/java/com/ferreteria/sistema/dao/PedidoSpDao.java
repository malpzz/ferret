package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Pedido;
import com.ferreteria.sistema.entity.Proveedor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class PedidoSpDao {
    private final JdbcTemplate jdbcTemplate;
    public PedidoSpDao(DataSource dataSource) { this.jdbcTemplate = new JdbcTemplate(dataSource); }

    private RowMapper<Pedido> mapper() {
        return new RowMapper<Pedido>() {
            @Override public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
                Pedido p = new Pedido();
                p.setIdPedido(rs.getLong("IDPEDIDO"));
                p.setNumeroPedido(rs.getString("NUMERO_PEDIDO"));
                java.sql.Timestamp ts = rs.getTimestamp("FECHA");
                if (ts != null) {
                    p.setFecha(ts.toLocalDateTime().toLocalDate());
                }
                p.setTotal(rs.getBigDecimal("TOTAL"));
                String estado = rs.getString("ESTADO");
                if (estado != null) {
                    String normalized = estado.trim().toUpperCase();
                    try { p.setEstado(Pedido.EstadoPedido.valueOf(normalized)); } catch (IllegalArgumentException ignored) {}
                }
                // DESCRIPCION puede no venir en el cursor
                try { p.setDescripcion(rs.getString("DESCRIPCION")); } catch (Exception ignored) {}
                // Mapear proveedor por ID/nombre si vienen en el cursor
                try {
                    Proveedor prov = p.getProveedor();
                    if (prov == null) prov = new Proveedor();
                    boolean setAny = false;
                    try {
                        long idProv = rs.getLong("IDPROVEEDOR");
                        if (!rs.wasNull()) {
                            prov.setIdProveedor(idProv);
                            setAny = true;
                        }
                    } catch (Exception ignoredInner) {}
                    try {
                        String nombreProv = rs.getString("NOMBREPROVEEDOR");
                        if (nombreProv != null) {
                            prov.setNombreProveedor(nombreProv);
                            setAny = true;
                        }
                    } catch (Exception ignoredInner) {}
                    if (setAny) p.setProveedor(prov);
                } catch (Exception ignored) {}
                java.sql.Timestamp fe = rs.getTimestamp("FECHA_ENTREGA_ESPERADA");
                if (fe != null) {
                    p.setFechaEntregaEsperada(fe.toLocalDateTime().toLocalDate());
                }
                return p;
            }
        };
    }

    public List<Pedido> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_COMPRAS").withFunctionName("FN_LISTAR_PEDIDOS")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked") List<Pedido> list = (List<Pedido>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Optional<Pedido> obtenerPorId(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_COMPRAS").withFunctionName("FN_OBTENER_PEDIDO")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
        @SuppressWarnings("unchecked") List<Pedido> list = (List<Pedido>) out.get("RETURN_VALUE");
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public Long crear(String numero, Date fecha, Long idProveedor, String estado, Date fechaEntrega, String descripcion, String observaciones) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_NUMERO", numero)
                .addValue("P_FECHA", new java.sql.Timestamp(fecha != null ? fecha.getTime() : System.currentTimeMillis()))
                .addValue("P_ID_PROVEEDOR", idProveedor)
                .addValue("P_ESTADO", estado)
                .addValue("P_FECHA_ENTREGA", fechaEntrega != null ? new java.sql.Timestamp(fechaEntrega.getTime()) : null)
                .addValue("P_DESCRIPCION", descripcion)
                .addValue("P_OBSERVACIONES", observaciones)
                .addValue("P_ID_PEDIDO", null);
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_COMPRAS").withProcedureName("SP_INSERTAR_PEDIDO");
        Map<String, Object> out = call.execute(in);
        Object id = out.get("P_ID_PEDIDO");
        if (id instanceof Number) return ((Number) id).longValue();
        return null;
    }

    public void agregarDetalle(Long idPedido, Long idProducto, java.math.BigDecimal precio, Integer cantidad) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_COMPRAS").withProcedureName("SP_AGREGAR_DETALLE_PEDIDO")
                .execute(new MapSqlParameterSource()
                        .addValue("P_ID_PEDIDO", idPedido)
                        .addValue("P_ID_PRODUCTO", idProducto)
                        .addValue("P_PRECIO", precio)
                        .addValue("P_CANTIDAD", cantidad)
                );
    }

    public void actualizarEstado(Long idPedido, String estado) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_COMPRAS").withProcedureName("SP_ACTUALIZAR_ESTADO_PEDIDO")
                .execute(new MapSqlParameterSource().addValue("P_ID", idPedido).addValue("P_ESTADO", estado));
    }
}



