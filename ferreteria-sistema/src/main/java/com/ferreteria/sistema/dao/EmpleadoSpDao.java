package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Empleado;
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
public class EmpleadoSpDao {
    private final JdbcTemplate jdbcTemplate;
    public EmpleadoSpDao(DataSource dataSource) { this.jdbcTemplate = new JdbcTemplate(dataSource); }

    private RowMapper<Empleado> mapper() {
        return new RowMapper<Empleado>() {
            @Override public Empleado mapRow(ResultSet rs, int rowNum) throws SQLException {
                Empleado e = new Empleado();
                e.setIdEmpleado(rs.getLong("IDEMPLEADO"));
                e.setNombreEmpleado(rs.getString("NOMBREEMPLEADO"));
                e.setApellidos(rs.getString("APELLIDOS"));
                e.setDireccion(rs.getString("DIRECCION"));
                e.setTelefono(rs.getString("TELEFONO"));
                e.setEmail(rs.getString("EMAIL"));
                e.setCedula(rs.getString("CEDULA"));
                e.setPuesto(rs.getString("PUESTO"));
                try { e.setSalario(rs.getBigDecimal("SALARIO")); } catch (SQLException ignored) {}
                try { e.setActivo(rs.getInt("ACTIVO") == 1); } catch (SQLException ignored) {}
                return e;
            }
        };
    }

    public List<Empleado> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withFunctionName("FN_LISTAR_EMPLEADOS")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked") List<Empleado> list = (List<Empleado>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Optional<Empleado> obtenerPorId(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withFunctionName("FN_OBTENER_EMPLEADO")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
        @SuppressWarnings("unchecked") List<Empleado> list = (List<Empleado>) out.get("RETURN_VALUE");
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public void insertar(Empleado e) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_INSERTAR_EMPLEADO")
                .execute(new MapSqlParameterSource()
                        .addValue("P_NOMBRE", e.getNombreEmpleado())
                        .addValue("P_APELLIDOS", e.getApellidos())
                        .addValue("P_DIRECCION", e.getDireccion())
                        .addValue("P_TELEFONO", e.getTelefono())
                        .addValue("P_EMAIL", e.getEmail())
                        .addValue("P_CEDULA", e.getCedula())
                        .addValue("P_PUESTO", e.getPuesto())
                        .addValue("P_SALARIO", e.getSalario())
                );
    }

    public void actualizar(Long id, Empleado e) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ACTUALIZAR_EMPLEADO")
                .execute(new MapSqlParameterSource()
                        .addValue("P_ID", id)
                        .addValue("P_NOMBRE", e.getNombreEmpleado())
                        .addValue("P_APELLIDOS", e.getApellidos())
                        .addValue("P_DIRECCION", e.getDireccion())
                        .addValue("P_TELEFONO", e.getTelefono())
                        .addValue("P_EMAIL", e.getEmail())
                        .addValue("P_CEDULA", e.getCedula())
                        .addValue("P_PUESTO", e.getPuesto())
                        .addValue("P_SALARIO", e.getSalario())
                );
    }

    public void eliminar(Long id) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ELIMINAR_EMPLEADO")
                .execute(new MapSqlParameterSource().addValue("P_ID", id));
    }
}




