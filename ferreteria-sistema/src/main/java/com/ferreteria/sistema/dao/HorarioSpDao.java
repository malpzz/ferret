package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Horario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.*;

@Repository
public class HorarioSpDao {
    private final JdbcTemplate jdbcTemplate;
    public HorarioSpDao(DataSource dataSource) { this.jdbcTemplate = new JdbcTemplate(dataSource); }

    private RowMapper<Horario> mapper() {
        return new RowMapper<Horario>() {
            @Override public Horario mapRow(ResultSet rs, int rowNum) throws SQLException {
                Horario h = new Horario();
                h.setIdHorario(rs.getLong("IDHORARIO"));
                h.setIdEmpleado(rs.getLong("IDEMPLEADO"));
                java.sql.Timestamp fecha = rs.getTimestamp("FECHA");
                if (fecha != null) h.setFecha(fecha.toLocalDateTime().toLocalDate());
                h.setHoraEntrada(rs.getBigDecimal("HORA_ENTRADA"));
                h.setHoraSalida(rs.getBigDecimal("HORA_SALIDA"));
                try { h.setHorasTrabajadas(rs.getBigDecimal("HORAS_TRABAJADAS")); } catch (SQLException ignored) {}
                h.setObservaciones(rs.getString("OBSERVACIONES"));
                return h;
            }
        };
    }

    public List<Horario> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_HORARIOS").withFunctionName("FN_LISTAR_HORARIOS")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked") List<Horario> list = (List<Horario>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Optional<Horario> obtenerPorId(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_HORARIOS").withFunctionName("FN_OBTENER_HORARIO")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID", id));
        @SuppressWarnings("unchecked") List<Horario> list = (List<Horario>) out.get("RETURN_VALUE");
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public List<Horario> listarPorEmpleado(Long idEmpleado) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_HORARIOS").withFunctionName("FN_LISTAR_HORARIOS_POR_EMPLEADO")
                .returningResultSet("RETURN_VALUE", mapper());
        Map<String, Object> out = call.execute(new MapSqlParameterSource().addValue("P_ID_EMPLEADO", idEmpleado));
        @SuppressWarnings("unchecked") List<Horario> list = (List<Horario>) out.get("RETURN_VALUE");
        return list != null ? list : Collections.emptyList();
    }

    public Long insertar(Horario h) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_ID_EMPLEADO", h.getIdEmpleado())
                .addValue("P_FECHA", java.sql.Date.valueOf(h.getFecha()))
                .addValue("P_HORA_ENTRADA", h.getHoraEntrada())
                .addValue("P_HORA_SALIDA", h.getHoraSalida())
                .addValue("P_OBSERVACIONES", h.getObservaciones())
                .addValue("P_ID_HORARIO", null);
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_HORARIOS").withProcedureName("SP_INSERTAR_HORARIO");
        Map<String, Object> out = call.execute(in);
        Object id = out.get("P_ID_HORARIO");
        if (id instanceof Number) return ((Number) id).longValue();
        return null;
    }

    public void actualizar(Long id, Horario h) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_HORARIOS").withProcedureName("SP_ACTUALIZAR_HORARIO")
                .execute(new MapSqlParameterSource()
                        .addValue("P_ID", id)
                        .addValue("P_FECHA", java.sql.Date.valueOf(h.getFecha()))
                        .addValue("P_HORA_ENTRADA", h.getHoraEntrada())
                        .addValue("P_HORA_SALIDA", h.getHoraSalida())
                        .addValue("P_OBSERVACIONES", h.getObservaciones())
                );
    }

    public void eliminar(Long id) {
        new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_HORARIOS").withProcedureName("SP_ELIMINAR_HORARIO")
                .execute(new MapSqlParameterSource().addValue("P_ID", id));
    }
}



