package com.inteliagent.auth.service;

import com.inteliagent.auth.entity.Cede;
import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.persistence.CedePersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CedeService {

    @Autowired
    private CedePersistence cedePersistence;

    public List<Usuario> validateCede(Integer codigo) throws Exception {
        return cedePersistence.findUsuariosCede(codigo);
    }
}
