package org.iclassq.service;

import jakarta.validation.ValidationException;
import org.iclassq.entity.TypeTask;
import org.iclassq.repository.TypeTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class TypeTaskServiceImpl implements TypeTaskService {
    private final Logger logger = Logger.getLogger(TypeTaskServiceImpl.class.getName());

    @Autowired
    private TypeTaskRepository typeTaskRepository;

    @Override
    public List<TypeTask> findAll() {
        return typeTaskRepository.findAll();
    }

    @Override
    public TypeTask findById(Integer id) {
        return typeTaskRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Tipo de tarea no encontrado"));
    }
}
