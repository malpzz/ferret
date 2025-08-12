package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Proveedor;
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
public class ProveedorSpDao {
    private final JdbcTemplate jdbcTemplate;
    public ProveedorSpDao(DataSource dataSource) { this.jdbcTemplate = new JdbcTemplate(dataSource); }

    private RowMapper<Proveedor> mapper() {
        return new RowMapper<Proveedor>() {
            @Override public Proveedor mapRow(ResultSet rs, int rowNum) throws SQLException {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getLong("IDPROVEEDOR"));
                p.setNombreProveedor(rs.getString("NOMBREPROVEEDOR"));
                p.setDireccion(rs.getString("DIRECCION"));
                p.setTelefono(rs.getString("TELEFONO"));
                p.setEmail(rs.getString("EMAIL"));
                p.setContactoPrincipal(rs.getString("CONTACTO_PRINCIPAL"));
                p.setRuc(rs.getString("RUC"));
                p.setCondicionesPago(rs.getString("CONDICIONES_PAGO"));
                try { p.setActivo(rs.getInt("ACTIVO") == 1); } catch (SQLException ignored) {}
                return p;
            }
        };
    }

    public List<Proveedor> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withFunctionName("FN_LISTAR_PROVEEDORES")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked") List<Proveedor> list = (List<Proveedor>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Optional<Proveedor> obtenerPorId(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA")
                .withFunctionName("FN_OBTENER_PROVEEDOR")
                .declareParameters(new SqlParameter("P_ID", Types.NUMERIC))
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
        @SuppressWarnings("unchecked") List<Proveedor> list = (List<Proveedor>) out.get("RETURN_VALUE");
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public void insertar(Proveedor p) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_INSERTAR_PROVEEDOR_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_NOMBRE", Types.VARCHAR),
                        new SqlParameter("P_DIRECCION", Types.VARCHAR),
                        new SqlParameter("P_TELEFONO", Types.VARCHAR),
                        new SqlParameter("P_EMAIL", Types.VARCHAR),
                        new SqlParameter("P_CONTACTO", Types.VARCHAR),
                        new SqlParameter("P_RUC", Types.VARCHAR),
                        new SqlParameter("P_CONDICIONES_PAGO", Types.VARCHAR)
                );
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_NOMBRE", p.getNombreProveedor())
                .addValue("P_DIRECCION", p.getDireccion())
                .addValue("P_TELEFONO", p.getTelefono())
                .addValue("P_EMAIL", p.getEmail())
                .addValue("P_CONTACTO", p.getContactoPrincipal())
                .addValue("P_RUC", p.getRuc())
                .addValue("P_CONDICIONES_PAGO", p.getCondicionesPago());
        call.execute(in);
    }

    public void actualizar(Long id, Proveedor p) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ACTUALIZAR_PROVEEDOR_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID", Types.NUMERIC),
                        new SqlParameter("P_NOMBRE", Types.VARCHAR),
                        new SqlParameter("P_DIRECCION", Types.VARCHAR),
                        new SqlParameter("P_TELEFONO", Types.VARCHAR),
                        new SqlParameter("P_EMAIL", Types.VARCHAR),
                        new SqlParameter("P_CONTACTO", Types.VARCHAR),
                        new SqlParameter("P_RUC", Types.VARCHAR),
                        new SqlParameter("P_CONDICIONES_PAGO", Types.VARCHAR)
                );
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_ID", id)
                .addValue("P_NOMBRE", p.getNombreProveedor())
                .addValue("P_DIRECCION", p.getDireccion())
                .addValue("P_TELEFONO", p.getTelefono())
                .addValue("P_EMAIL", p.getEmail())
                .addValue("P_CONTACTO", p.getContactoPrincipal())
                .addValue("P_RUC", p.getRuc())
                .addValue("P_CONDICIONES_PAGO", p.getCondicionesPago());
        call.execute(in);
    }

    public void eliminar(Long id) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ELIMINAR_PROVEEDOR_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("P_ID", Types.NUMERIC))
                .execute(new MapSqlParameterSource().addValue("P_ID", id));
    }
}




