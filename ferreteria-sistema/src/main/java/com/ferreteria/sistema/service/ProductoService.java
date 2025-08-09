package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.ProductoSpDao;
import com.ferreteria.sistema.entity.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoSpDao productoSpDao;

    public List<Producto> obtenerTodos() { return productoSpDao.listar(); }

    public Optional<Producto> obtenerPorId(Long id) { return productoSpDao.obtenerPorId(id); }

    public void crear(Producto p) { productoSpDao.insertar(p); }

    public void actualizar(Long id, Producto p) { productoSpDao.actualizar(id, p); }

    public void eliminar(Long id) { productoSpDao.eliminar(id); }
}




