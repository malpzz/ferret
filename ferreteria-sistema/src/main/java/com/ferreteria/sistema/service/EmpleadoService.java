package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.EmpleadoSpDao;
import com.ferreteria.sistema.entity.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmpleadoService {

    @Autowired
    private EmpleadoSpDao empleadoSpDao;

    public List<Empleado> obtenerTodos() { return empleadoSpDao.listar(); }

    public Optional<Empleado> obtenerPorId(Long id) { return empleadoSpDao.obtenerPorId(id); }

    public void crear(Empleado e) { empleadoSpDao.insertar(e); }

    public void actualizar(Long id, Empleado e) { empleadoSpDao.actualizar(id, e); }

    public void eliminar(Long id) { empleadoSpDao.eliminar(id); }
}




