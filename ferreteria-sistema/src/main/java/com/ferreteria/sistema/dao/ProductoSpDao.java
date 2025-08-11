package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Producto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.SqlParameter;
import java.sql.Types;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ProductoSpDao {
    private final JdbcTemplate jdbcTemplate;

    public ProductoSpDao(DataSource dataSource) { this.jdbcTemplate = new JdbcTemplate(dataSource); }

    private RowMapper<Producto> mapper() {
        return new RowMapper<Producto>() {
            @Override public Producto mapRow(ResultSet rs, int rowNum) throws SQLException {
                Producto p = new Producto();
                p.setIdProducto(rs.getLong("IDPRODUCTO"));
                p.setNombreProducto(rs.getString("NOMBREPRODUCTO"));
                p.setDescripcion(rs.getString("DESCRIPCION"));
                p.setCodigoProducto(rs.getString("CODIGO_PRODUCTO"));
                p.setCategoria(rs.getString("CATEGORIA"));
                p.setMarca(rs.getString("MARCA"));
                p.setPrecio(rs.getBigDecimal("PRECIO"));
                try { p.setPrecioCompra(rs.getBigDecimal("PRECIO_COMPRA")); } catch (Exception ignored) {}
                p.setUnidadMedida(rs.getString("UNIDAD_MEDIDA"));
                try { p.setStockMinimo(rs.getInt("STOCK_MINIMO")); } catch (Exception ignored) {}
                try { p.setActivo(rs.getInt("ACTIVO") == 1); } catch (Exception ignored) {}
                
                // Mapear proveedor - CRÍTICO para que aparezca en frontend
                try {
                    Long idProveedor = rs.getLong("IDPROVEEDOR");
                    if (idProveedor != null && idProveedor > 0) {
                        com.ferreteria.sistema.entity.Proveedor proveedor = new com.ferreteria.sistema.entity.Proveedor();
                        proveedor.setIdProveedor(idProveedor);
                        
                        // Si viene el nombre del proveedor en el cursor, asignarlo
                        try {
                            String nombreProveedor = rs.getString("NOMBREPROVEEDOR");
                            if (nombreProveedor != null) {
                                proveedor.setNombreProveedor(nombreProveedor);
                            }
                        } catch (Exception ignored) {}
                        
                        p.setProveedor(proveedor);
                    }
                } catch (Exception ignored) {}
                
                return p;
            }
        };
    }

    public List<Producto> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withFunctionName("FN_LISTAR_PRODUCTOS")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked") List<Producto> list = (List<Producto>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA").withFunctionName("FN_OBTENER_PRODUCTO")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(new SqlParameter("P_ID", Types.NUMERIC))
                    .returningResultSet("RETURN_VALUE", mapper());
            Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
            @SuppressWarnings("unchecked") List<Producto> list = (List<Producto>) out.get("RETURN_VALUE");
            if (list == null || list.isEmpty()) return Optional.empty();
            return Optional.of(list.get(0));
        } catch (Exception ex) {
            // Fallback: usa listado y filtra
            List<Producto> all = listar();
            return all.stream().filter(p -> Objects.equals(p.getIdProducto(), id)).findFirst();
        }
    }

    public void insertar(Producto p) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_INSERTAR_PRODUCTO_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_NOMBRE", Types.VARCHAR),
                        new SqlParameter("P_DESCRIPCION", Types.VARCHAR),
                        new SqlParameter("P_CODIGO", Types.VARCHAR),
                        new SqlParameter("P_CATEGORIA", Types.VARCHAR),
                        new SqlParameter("P_MARCA", Types.VARCHAR),
                        new SqlParameter("P_PRECIO", Types.NUMERIC),
                        new SqlParameter("P_PRECIO_COMPRA", Types.NUMERIC),
                        new SqlParameter("P_UNIDAD_MEDIDA", Types.VARCHAR),
                        new SqlParameter("P_STOCK_MINIMO", Types.NUMERIC),
                        new SqlParameter("P_ID_PROVEEDOR", Types.NUMERIC)
                );
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_NOMBRE", p.getNombreProducto())
                .addValue("P_DESCRIPCION", p.getDescripcion())
                .addValue("P_CODIGO", p.getCodigoProducto())
                .addValue("P_CATEGORIA", p.getCategoria())
                .addValue("P_MARCA", p.getMarca())
                .addValue("P_PRECIO", p.getPrecio())
                .addValue("P_PRECIO_COMPRA", p.getPrecioCompra())
                .addValue("P_UNIDAD_MEDIDA", p.getUnidadMedida())
                .addValue("P_STOCK_MINIMO", p.getStockMinimo())
                .addValue("P_ID_PROVEEDOR", p.getProveedor() != null ? p.getProveedor().getIdProveedor() : null);
        call.execute(in);
    }

    public void actualizar(Long id, Producto p) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ACTUALIZAR_PRODUCTO_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID", Types.NUMERIC),
                        new SqlParameter("P_NOMBRE", Types.VARCHAR),
                        new SqlParameter("P_DESCRIPCION", Types.VARCHAR),
                        new SqlParameter("P_CODIGO", Types.VARCHAR),
                        new SqlParameter("P_CATEGORIA", Types.VARCHAR),
                        new SqlParameter("P_MARCA", Types.VARCHAR),
                        new SqlParameter("P_PRECIO", Types.NUMERIC),
                        new SqlParameter("P_PRECIO_COMPRA", Types.NUMERIC),
                        new SqlParameter("P_UNIDAD_MEDIDA", Types.VARCHAR),
                        new SqlParameter("P_STOCK_MINIMO", Types.NUMERIC),
                        new SqlParameter("P_ID_PROVEEDOR", Types.NUMERIC)
                );
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_ID", id)
                .addValue("P_NOMBRE", p.getNombreProducto())
                .addValue("P_DESCRIPCION", p.getDescripcion())
                .addValue("P_CODIGO", p.getCodigoProducto())
                .addValue("P_CATEGORIA", p.getCategoria())
                .addValue("P_MARCA", p.getMarca())
                .addValue("P_PRECIO", p.getPrecio())
                .addValue("P_PRECIO_COMPRA", p.getPrecioCompra())
                .addValue("P_UNIDAD_MEDIDA", p.getUnidadMedida())
                .addValue("P_STOCK_MINIMO", p.getStockMinimo())
                .addValue("P_ID_PROVEEDOR", p.getProveedor() != null ? p.getProveedor().getIdProveedor() : null);
        call.execute(in);
    }

    public void eliminar(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ELIMINAR_PRODUCTO_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("P_ID", Types.NUMERIC));
        call.execute(new MapSqlParameterSource().addValue("P_ID", id));
    }
}




