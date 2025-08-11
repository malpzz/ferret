package com.ferreteria.sistema.dao;

import com.ferreteria.sistema.entity.Horario;
import com.ferreteria.sistema.entity.Empleado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private RowMapper<Horario> mapperConEmpleado() {
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
                
                // Mapear empleado si existe
                try {
                    String nombreEmpleado = rs.getString("NOMBRE_EMPLEADO");
                    if (nombreEmpleado != null) {
                        Empleado empleado = new Empleado();
                        empleado.setIdEmpleado(rs.getLong("IDEMPLEADO"));
                        empleado.setNombreEmpleado(nombreEmpleado);
                        empleado.setApellidos(rs.getString("APELLIDOS"));
                        empleado.setPuesto(rs.getString("PUESTO"));
                        h.setEmpleado(empleado);
                    }
                } catch (SQLException ignored) {
                    // Ignorar si no hay datos del empleado
                }
                return h;
            }
        };
    }

    public List<Horario> listar() {
        try {
            System.out.println("DEBUG - Iniciando consulta de horarios...");
            
            // Primero intentar una consulta simple para verificar la estructura
            String simpleSql = "SELECT COUNT(*) FROM HORARIOS";
            Integer count = jdbcTemplate.queryForObject(simpleSql, Integer.class);
            System.out.println("DEBUG - Hay " + count + " registros en HORARIOS");
            
            // Verificar también empleados
            String empSql = "SELECT COUNT(*) FROM EMPLEADOS";
            Integer empCount = jdbcTemplate.queryForObject(empSql, Integer.class);
            System.out.println("DEBUG - Hay " + empCount + " registros en EMPLEADOS");
            
            // Consulta con JOIN para incluir empleados
            String sqlConJoin = """
                SELECT h.IDHORARIO, h.IDEMPLEADO, h.FECHA, h.HORA_ENTRADA, h.HORA_SALIDA, 
                       h.OBSERVACIONES,
                       e.NOMBRE_EMPLEADO, e.APELLIDOS, e.PUESTO
                FROM HORARIOS h 
                LEFT JOIN EMPLEADOS e ON h.IDEMPLEADO = e.IDEMPLEADO
                ORDER BY h.FECHA DESC, h.IDHORARIO DESC
                """;
            
            System.out.println("DEBUG - Ejecutando consulta con JOIN...");
            List<Horario> result = jdbcTemplate.query(sqlConJoin, mapperConEmpleado());
            System.out.println("DEBUG - Resultado: " + result.size() + " horarios encontrados");
            
            return result;
            
        } catch (Exception e) {
            System.out.println("ERROR en listar horarios: " + e.getMessage());
            e.printStackTrace();
            
            // Si falla el JOIN, intentar sin él
            try {
                System.out.println("DEBUG - Intentando consulta básica como fallback...");
                String basicSql = """
                    SELECT h.IDHORARIO, h.IDEMPLEADO, h.FECHA, h.HORA_ENTRADA, h.HORA_SALIDA, 
                           h.OBSERVACIONES
                    FROM HORARIOS h 
                    ORDER BY h.FECHA DESC, h.IDHORARIO DESC
                    """;
                return jdbcTemplate.query(basicSql, mapper());
            } catch (Exception e2) {
                System.out.println("ERROR también en consulta básica: " + e2.getMessage());
                throw e2;
            }
        }
    }

    public Optional<Horario> obtenerPorId(Long id) {
        String sql = """
            SELECT h.IDHORARIO, h.IDEMPLEADO, h.FECHA, h.HORA_ENTRADA, h.HORA_SALIDA, 
                   h.OBSERVACIONES, h.HORAS_TRABAJADAS 
            FROM HORARIOS h 
            WHERE h.IDHORARIO = ?
            """;
        List<Horario> list = jdbcTemplate.query(sql, mapper(), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Horario> listarPorEmpleado(Long idEmpleado) {
        String sql = """
            SELECT h.IDHORARIO, h.IDEMPLEADO, h.FECHA, h.HORA_ENTRADA, h.HORA_SALIDA, 
                   h.OBSERVACIONES, h.HORAS_TRABAJADAS,
                   e.NOMBRE_EMPLEADO, e.APELLIDOS, e.PUESTO
            FROM HORARIOS h 
            LEFT JOIN EMPLEADOS e ON h.IDEMPLEADO = e.IDEMPLEADO
            WHERE h.IDEMPLEADO = ?
            ORDER BY h.FECHA DESC
            """;
        return jdbcTemplate.query(sql, mapperConEmpleado(), idEmpleado);
    }

    public Long insertar(Horario h) {
        try {
            System.out.println("DEBUG - Insertando horario...");
            System.out.println("DEBUG - ID Empleado: " + h.getIdEmpleado());
            System.out.println("DEBUG - Fecha: " + h.getFecha());
            System.out.println("DEBUG - Hora Entrada: " + h.getHoraEntrada());
            System.out.println("DEBUG - Hora Salida: " + h.getHoraSalida());
            System.out.println("DEBUG - Observaciones: " + h.getObservaciones());
            
            // Validaciones básicas
            if (h.getIdEmpleado() == null) {
                throw new IllegalArgumentException("ID de empleado no puede ser null");
            }
            if (h.getFecha() == null) {
                throw new IllegalArgumentException("Fecha no puede ser null");
            }
            if (h.getHoraEntrada() == null) {
                throw new IllegalArgumentException("Hora de entrada no puede ser null");
            }
            if (h.getHoraSalida() == null) {
                throw new IllegalArgumentException("Hora de salida no puede ser null");
            }
            
            // Intentar inserción sin FECHA_CREACION primero
            String sql = """
                INSERT INTO HORARIOS (IDEMPLEADO, FECHA, HORA_ENTRADA, HORA_SALIDA, OBSERVACIONES)
                VALUES (?, ?, ?, ?, ?)
                """;
            
            System.out.println("DEBUG - Ejecutando INSERT...");
            jdbcTemplate.update(sql, 
                h.getIdEmpleado(),
                java.sql.Date.valueOf(h.getFecha()),
                h.getHoraEntrada(),
                h.getHoraSalida(),
                h.getObservaciones()
            );
            
            System.out.println("DEBUG - INSERT exitoso, obteniendo ID...");
            
            // Obtener el ID generado usando diferentes estrategias
            try {
                String selectId = "SELECT HORARIOS_SEQ.CURRVAL FROM DUAL";
                Long id = jdbcTemplate.queryForObject(selectId, Long.class);
                System.out.println("DEBUG - ID obtenido de secuencia: " + id);
                return id;
            } catch (Exception e1) {
                System.out.println("DEBUG - Error con secuencia, intentando MAX...");
                try {
                    String altQuery = """
                        SELECT MAX(IDHORARIO) FROM HORARIOS 
                        WHERE IDEMPLEADO = ? AND FECHA = ? AND HORA_ENTRADA = ? AND HORA_SALIDA = ?
                        """;
                    Long id = jdbcTemplate.queryForObject(altQuery, Long.class,
                        h.getIdEmpleado(), java.sql.Date.valueOf(h.getFecha()), 
                        h.getHoraEntrada(), h.getHoraSalida());
                    System.out.println("DEBUG - ID obtenido con MAX: " + id);
                    return id;
                } catch (Exception e2) {
                    System.out.println("DEBUG - Error también con MAX, usando estrategia simple...");
                    String simpleQuery = "SELECT MAX(IDHORARIO) FROM HORARIOS";
                    Long id = jdbcTemplate.queryForObject(simpleQuery, Long.class);
                    System.out.println("DEBUG - ID obtenido simple: " + id);
                    return id;
                }
            }
            
        } catch (Exception e) {
            System.out.println("ERROR insertando horario: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void actualizar(Long id, Horario h) {
        String sql = """
            UPDATE HORARIOS 
            SET FECHA = ?, HORA_ENTRADA = ?, HORA_SALIDA = ?, OBSERVACIONES = ?
            WHERE IDHORARIO = ?
            """;
        
        jdbcTemplate.update(sql, 
            java.sql.Date.valueOf(h.getFecha()),
            h.getHoraEntrada(),
            h.getHoraSalida(),
            h.getObservaciones(),
            id
        );
    }

    public void eliminar(Long id) {
        String sql = "DELETE FROM HORARIOS WHERE IDHORARIO = ?";
        jdbcTemplate.update(sql, id);
    }
}



