package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.PedidoSpDao;
import com.ferreteria.sistema.entity.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoSpDao pedidoSpDao;

    public List<Pedido> obtenerTodos() { return pedidoSpDao.listar(); }

    public Optional<Pedido> obtenerPorId(Long id) { return pedidoSpDao.obtenerPorId(id); }

    public Long crear(String numero, Date fecha, Long idProveedor, String estado, Date fechaEntrega, String descripcion, String observaciones) {
        return pedidoSpDao.crear(numero, fecha, idProveedor, estado, fechaEntrega, descripcion, observaciones);
    }

    public void actualizarEstado(Long id, String estado) { pedidoSpDao.actualizarEstado(id, estado); }
}




