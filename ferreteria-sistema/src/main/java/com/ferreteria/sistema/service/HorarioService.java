package com.ferreteria.sistema.service;

import com.ferreteria.sistema.dao.HorarioSpDao;
import com.ferreteria.sistema.entity.Horario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HorarioService {

    @Autowired
    private HorarioSpDao horarioSpDao;

    public List<Horario> obtenerTodos() { return horarioSpDao.listar(); }

    public Optional<Horario> obtenerPorId(Long id) { return horarioSpDao.obtenerPorId(id); }

    public List<Horario> listarPorEmpleado(Long idEmpleado) { return horarioSpDao.listarPorEmpleado(idEmpleado); }

    public Long crear(Horario h) { return horarioSpDao.insertar(h); }

    public void actualizar(Long id, Horario h) { horarioSpDao.actualizar(id, h); }

    public void eliminar(Long id) { horarioSpDao.eliminar(id); }
}




