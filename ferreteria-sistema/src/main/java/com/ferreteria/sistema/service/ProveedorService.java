package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.ProveedorSpDao;
import com.ferreteria.sistema.entity.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProveedorService {

    @Autowired
    private ProveedorSpDao proveedorSpDao;

    public List<Proveedor> obtenerTodos() { return proveedorSpDao.listar(); }

    public Optional<Proveedor> obtenerPorId(Long id) { return proveedorSpDao.obtenerPorId(id); }

    public void crear(Proveedor p) { proveedorSpDao.insertar(p); }

    public void actualizar(Long id, Proveedor p) { proveedorSpDao.actualizar(id, p); }

    public void eliminar(Long id) { proveedorSpDao.eliminar(id); }
}




