package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Empleado;
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
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA").withFunctionName("fn_listar_empleados")
                    .returningResultSet("RETURN_VALUE", mapper());
            Map<String, Object> out = call.execute(new HashMap<>());
            @SuppressWarnings("unchecked") List<Empleado> list = (List<Empleado>) out.get("RETURN_VALUE");
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            // Fallback: usar consulta SQL directa
            String sql = "SELECT IdEmpleado, nombreEmpleado, apellidos, direccion, telefono, email, cedula, puesto, salario, activo, fecha_ingreso, fecha_modificacion FROM Empleados ORDER BY nombreEmpleado, apellidos";
            return jdbcTemplate.query(sql, mapper());
        }
    }

    public Optional<Empleado> obtenerPorId(Long id) {
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA").withFunctionName("fn_obtener_empleado")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(new SqlParameter("P_ID", Types.NUMERIC))
                    .returningResultSet("RETURN_VALUE", mapper());
            Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
            @SuppressWarnings("unchecked") List<Empleado> list = (List<Empleado>) out.get("RETURN_VALUE");
            if (list == null || list.isEmpty()) return Optional.empty();
            return Optional.of(list.get(0));
        } catch (Exception e) {
            // Fallback: usar consulta SQL directa
            String sql = "SELECT IdEmpleado, nombreEmpleado, apellidos, direccion, telefono, email, cedula, puesto, salario, activo, fecha_ingreso, fecha_modificacion FROM Empleados WHERE IdEmpleado = ?";
            List<Empleado> list = jdbcTemplate.query(sql, mapper(), id);
            if (list.isEmpty()) return Optional.empty();
            return Optional.of(list.get(0));
        }
    }

    public void insertar(Empleado e) {
        try {
            new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_INSERTAR_EMPLEADO_JDBC")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_NOMBRE", Types.VARCHAR),
                            new SqlParameter("P_APELLIDOS", Types.VARCHAR),
                            new SqlParameter("P_DIRECCION", Types.VARCHAR),
                            new SqlParameter("P_TELEFONO", Types.VARCHAR),
                            new SqlParameter("P_EMAIL", Types.VARCHAR),
                            new SqlParameter("P_CEDULA", Types.VARCHAR),
                            new SqlParameter("P_PUESTO", Types.VARCHAR),
                            new SqlParameter("P_SALARIO", Types.NUMERIC)
                    )
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
        } catch (Exception ex) {
            // Fallback: usar insert directo
            String sql = "INSERT INTO Empleados (nombreEmpleado, apellidos, direccion, telefono, email, cedula, puesto, salario, activo, fecha_ingreso, fecha_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, SYSDATE, SYSDATE)";
            jdbcTemplate.update(sql, 
                e.getNombreEmpleado(), e.getApellidos(), e.getDireccion(), 
                e.getTelefono(), e.getEmail(), e.getCedula(), 
                e.getPuesto(), e.getSalario());
        }
    }

    public void actualizar(Long id, Empleado e) {
        try {
            new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ACTUALIZAR_EMPLEADO_JDBC")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_ID", Types.NUMERIC),
                            new SqlParameter("P_NOMBRE", Types.VARCHAR),
                            new SqlParameter("P_APELLIDOS", Types.VARCHAR),
                            new SqlParameter("P_DIRECCION", Types.VARCHAR),
                            new SqlParameter("P_TELEFONO", Types.VARCHAR),
                            new SqlParameter("P_EMAIL", Types.VARCHAR),
                            new SqlParameter("P_CEDULA", Types.VARCHAR),
                            new SqlParameter("P_PUESTO", Types.VARCHAR),
                            new SqlParameter("P_SALARIO", Types.NUMERIC)
                    )
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
        } catch (Exception ex) {
            // Fallback: usar update directo
            String sql = "UPDATE Empleados SET nombreEmpleado=?, apellidos=?, direccion=?, telefono=?, email=?, cedula=?, puesto=?, salario=?, fecha_modificacion=SYSDATE WHERE IdEmpleado=?";
            jdbcTemplate.update(sql, 
                e.getNombreEmpleado(), e.getApellidos(), e.getDireccion(), 
                e.getTelefono(), e.getEmail(), e.getCedula(), 
                e.getPuesto(), e.getSalario(), id);
        }
    }

    public void eliminar(Long id) {
        try {
            new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA").withProcedureName("SP_ELIMINAR_EMPLEADO_JDBC")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(new SqlParameter("P_ID", Types.NUMERIC))
                    .execute(new MapSqlParameterSource().addValue("P_ID", id));
        } catch (Exception ex) {
            // Fallback: usar delete directo pero verificar restricciones
            int horarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Horarios WHERE IdEmpleado = ?", Integer.class, id);
            if (horarios > 0) {
                throw new RuntimeException("No se puede eliminar el empleado porque tiene horarios asociados");
            }
            jdbcTemplate.update("DELETE FROM Empleados WHERE IdEmpleado = ?", id);
        }
    }
}




