package com.ferreteria.sistema.repository;

import com.ferreteria.sistema.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * 
 * Proporciona métodos de acceso a datos para la gestión de usuarios
 * del sistema de ferretería.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario
     * @param nombreUsuario el nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Busca un usuario por su email
     * @param email el email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por nombre de usuario o email
     * @param nombreUsuario el nombre de usuario
     * @param email el email
     * @return Optional con el usuario si existe
     */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.nombreUsuario = :nombreUsuario OR u.email = :email")
    Optional<Usuario> findByNombreUsuarioOrEmail(@Param("nombreUsuario") String nombreUsuario,
                                                 @Param("email") String email);

    /**
     * Obtiene todos los usuarios activos
     * @return lista de usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Obtiene todos los usuarios inactivos
     * @return lista de usuarios inactivos
     */
    List<Usuario> findByActivoFalse();

    /**
     * Obtiene usuarios por rol
     * @param idRol el ID del rol
     * @return lista de usuarios con el rol especificado
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.idRol = :idRol")
    List<Usuario> findByRolId(@Param("idRol") Long idRol);

    /**
     * Obtiene usuarios activos por rol
     * @param idRol el ID del rol
     * @return lista de usuarios activos con el rol especificado
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.idRol = :idRol AND u.activo = true")
    List<Usuario> findByRolIdAndActivoTrue(@Param("idRol") Long idRol);

    /**
     * Busca usuarios por nombre o apellidos
     * @param texto texto a buscar
     * @return lista de usuarios que coinciden
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "UPPER(u.nombre) LIKE UPPER(CONCAT('%', :texto, '%')) OR " +
           "UPPER(u.apellidos) LIKE UPPER(CONCAT('%', :texto, '%')) OR " +
           "UPPER(u.nombreUsuario) LIKE UPPER(CONCAT('%', :texto, '%'))")
    List<Usuario> buscarPorNombreOApellidos(@Param("texto") String texto);

    /**
     * Obtiene usuarios que no han accedido en los últimos días especificados
     * @param fecha fecha límite
     * @return lista de usuarios inactivos
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND " +
           "(u.ultimoAcceso IS NULL OR u.ultimoAcceso < :fecha)")
    List<Usuario> findUsuariosInactivosDesdeFecha(@Param("fecha") LocalDateTime fecha);

    /**
     * Obtiene usuarios con intentos fallidos superiores al límite
     * @param limite número máximo de intentos
     * @return lista de usuarios con muchos intentos fallidos
     */
    @Query("SELECT u FROM Usuario u WHERE u.intentosFallidos >= :limite AND u.activo = true")
    List<Usuario> findUsuariosConIntentosExcesivos(@Param("limite") Integer limite);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado
     * @param nombreUsuario el nombre de usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Verifica si existe un usuario con el email especificado
     * @param email el email
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario, excluyendo un ID específico
     * @param nombreUsuario el nombre de usuario
     * @param idUsuario el ID del usuario a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreUsuarioAndIdUsuarioNot(String nombreUsuario, Long idUsuario);

    /**
     * Verifica si existe un usuario con el email, excluyendo un ID específico
     * @param email el email
     * @param idUsuario el ID del usuario a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmailAndIdUsuarioNot(String email, Long idUsuario);

    /**
     * Cuenta usuarios activos por rol
     * @param nombreRol el nombre del rol
     * @return cantidad de usuarios activos
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = :nombreRol AND u.activo = true")
    Long countByRolNombreAndActivoTrue(@Param("nombreRol") String nombreRol);

    /**
     * Obtiene el último usuario creado
     * @return Optional con el último usuario
     */
    @Query("SELECT u FROM Usuario u ORDER BY u.fechaCreacion DESC")
    List<Usuario> findTopByOrderByFechaCreacionDesc();

    /**
     * Busca usuarios con emails que coincidan con un patrón regex
     * @param patron patrón de expresión regular
     * @return lista de usuarios que coinciden
     */
    @Query(value = "SELECT * FROM USUARIOS u WHERE REGEXP_LIKE(u.EMAIL, ?1, 'i')", nativeQuery = true)
    List<Usuario> findByEmailMatchingPattern(String patron);
}

