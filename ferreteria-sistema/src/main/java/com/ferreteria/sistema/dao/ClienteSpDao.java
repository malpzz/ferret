package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Cliente;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZoneId;
import java.util.*;

@Repository
public class ClienteSpDao {

    private final JdbcTemplate jdbcTemplate;

    public ClienteSpDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private RowMapper<Cliente> clienteRowMapper() {
        return new RowMapper<Cliente>() {
            @Override
            public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getLong("IDCLIENTE"));
                c.setNombreCliente(rs.getString("NOMBRECLIENTE"));
                c.setApellidos(rs.getString("APELLIDOS"));
                c.setDireccion(rs.getString("DIRECCION"));
                c.setTelefono(rs.getString("TELEFONO"));
                c.setEmail(rs.getString("EMAIL"));
                c.setCedula(rs.getString("CEDULA"));
                String tipo = rs.getString("TIPO_CLIENTE");
                if (tipo != null) {
                    try { c.setTipoCliente(Cliente.TipoCliente.valueOf(tipo)); } catch (IllegalArgumentException ignored) {}
                }
                try {
                    c.setActivo(rs.getInt("ACTIVO") == 1);
                } catch (SQLException e) {
                    c.setActivo(true);
                }
                java.sql.Timestamp fr = rs.getTimestamp("FECHA_REGISTRO");
                if (fr != null) c.setFechaRegistro(fr.toLocalDateTime());
                java.sql.Timestamp fm = rs.getTimestamp("FECHA_MODIFICACION");
                if (fm != null) c.setFechaModificacion(fm.toLocalDateTime());
                return c;
            }
        };
    }

    public List<Cliente> listar() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA")
                .withFunctionName("fn_listar_clientes")
                .returningResultSet("RETURN_VALUE", clienteRowMapper());

        Map<String, Object> out = call.execute(new HashMap<>());
        @SuppressWarnings("unchecked")
        List<Cliente> lista = (List<Cliente>) out.get("RETURN_VALUE");
        return lista != null ? lista : Collections.emptyList();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withCatalogName("PKG_FERRETERIA")
                    .withFunctionName("fn_obtener_cliente")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_ID", Types.NUMERIC)
                    )
                    .returningResultSet("RETURN_VALUE", clienteRowMapper());

            MapSqlParameterSource in = new MapSqlParameterSource().addValue("P_ID", id, Types.NUMERIC);
            Map<String, Object> out = call.execute(in);
            @SuppressWarnings("unchecked")
            List<Cliente> lista = (List<Cliente>) out.get("RETURN_VALUE");
            if (lista == null || lista.isEmpty()) return Optional.empty();
            return Optional.of(lista.get(0));
        } catch (Exception e) {
            // Fallback: buscar en la lista completa
            List<Cliente> todos = listar();
            return todos.stream()
                    .filter(c -> c.getIdCliente().equals(id))
                    .findFirst();
        }
    }

    public void insertar(Cliente c) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA")
                .withProcedureName("SP_INSERTAR_CLIENTE_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_NOMBRE", Types.VARCHAR),
                        new SqlParameter("P_APELLIDOS", Types.VARCHAR),
                        new SqlParameter("P_DIRECCION", Types.VARCHAR),
                        new SqlParameter("P_TELEFONO", Types.VARCHAR),
                        new SqlParameter("P_EMAIL", Types.VARCHAR),
                        new SqlParameter("P_CEDULA", Types.VARCHAR),
                        new SqlParameter("P_TIPO_CLIENTE", Types.VARCHAR)
                );

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_NOMBRE", c.getNombreCliente())
                .addValue("P_APELLIDOS", c.getApellidos())
                .addValue("P_DIRECCION", c.getDireccion())
                .addValue("P_TELEFONO", c.getTelefono())
                .addValue("P_EMAIL", c.getEmail())
                .addValue("P_CEDULA", c.getCedula())
                .addValue("P_TIPO_CLIENTE", c.getTipoCliente() != null ? c.getTipoCliente().name() : "REGULAR");

        call.execute(in);
    }

    public void actualizar(Long id, Cliente c) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA")
                .withProcedureName("SP_ACTUALIZAR_CLIENTE_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID", Types.NUMERIC),
                        new SqlParameter("P_NOMBRE", Types.VARCHAR),
                        new SqlParameter("P_APELLIDOS", Types.VARCHAR),
                        new SqlParameter("P_DIRECCION", Types.VARCHAR),
                        new SqlParameter("P_TELEFONO", Types.VARCHAR),
                        new SqlParameter("P_EMAIL", Types.VARCHAR),
                        new SqlParameter("P_CEDULA", Types.VARCHAR),
                        new SqlParameter("P_TIPO_CLIENTE", Types.VARCHAR)
                );

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("P_ID", id)
                .addValue("P_NOMBRE", c.getNombreCliente())
                .addValue("P_APELLIDOS", c.getApellidos())
                .addValue("P_DIRECCION", c.getDireccion())
                .addValue("P_TELEFONO", c.getTelefono())
                .addValue("P_EMAIL", c.getEmail())
                .addValue("P_CEDULA", c.getCedula())
                .addValue("P_TIPO_CLIENTE", c.getTipoCliente() != null ? c.getTipoCliente().name() : null);

        call.execute(in);
    }

    public void eliminar(Long id) throws DataAccessException {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FERRETERIA")
                .withProcedureName("SP_ELIMINAR_CLIENTE_JDBC")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID", Types.NUMERIC)
                );

        MapSqlParameterSource in = new MapSqlParameterSource().addValue("P_ID", id);
        call.execute(in);
    }
}



