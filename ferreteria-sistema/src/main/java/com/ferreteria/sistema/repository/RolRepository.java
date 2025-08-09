package com.ferreteria.sistema.repository;

import com.ferreteria.sistema.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Rol
 * 
 * Proporciona métodos de acceso a datos para la gestión de roles
 * del sistema de ferretería.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre
     * @param nombre el nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Busca un rol por su nombre ignorando mayúsculas y minúsculas
     * @param nombre el nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombreIgnoreCase(String nombre);

    /**
     * Obtiene todos los roles activos
     * @return lista de roles activos
     */
    List<Rol> findByActivoTrue();

    /**
     * Obtiene todos los roles inactivos
     * @return lista de roles inactivos
     */
    List<Rol> findByActivoFalse();

    /**
     * Busca roles por nombre que contenga el texto especificado
     * @param nombre texto a buscar en el nombre
     * @return lista de roles que coinciden
     */
    @Query("SELECT r FROM Rol r WHERE UPPER(r.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Rol> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Verifica si existe un rol con el nombre especificado
     * @param nombre el nombre del rol
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica si existe un rol con el nombre especificado, excluyendo un ID específico
     * @param nombre el nombre del rol
     * @param idRol el ID del rol a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreAndIdRolNot(String nombre, Long idRol);

    /**
     * Cuenta la cantidad de usuarios activos por rol
     * @param idRol el ID del rol
     * @return cantidad de usuarios activos
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.idRol = :idRol AND u.activo = true")
    Long countUsuariosActivosByRol(@Param("idRol") Long idRol);

    /**
     * Obtiene roles ordenados por nombre
     * @return lista de roles ordenados alfabéticamente
     */
    List<Rol> findAllByOrderByNombreAsc();
}

