package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.FacturaSpDao;
import com.ferreteria.sistema.entity.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FacturaService {

    @Autowired
    private FacturaSpDao facturaSpDao;

    public List<Factura> obtenerTodos() { return facturaSpDao.listar(); }

    public Optional<Factura> obtenerPorId(Long id) { return facturaSpDao.obtenerPorId(id); }

    public Long crearBasica(String numero, Date fecha, Long idCliente, String metodoPago, String estado, String obs) {
        return facturaSpDao.crearFacturaBasica(numero, fecha, idCliente, metodoPago, estado, obs);
    }

    public void anular(Long id) { facturaSpDao.anular(id); }
}




