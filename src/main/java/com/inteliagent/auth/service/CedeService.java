package com.inteliagent.auth.service;

import com.inteliagent.auth.entity.Cede;
import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.persistence.CedePersistence;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CedeService {
    public CedeService(CedePersistence cedePersistence) {
        this.cedePersistence = cedePersistence;
    }


    private final CedePersistence cedePersistence;

    public List<Usuario> validateCede(Integer codigo) throws Exception {
        return cedePersistence.findUsuariosCede(codigo);
    }
}
