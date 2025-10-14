package org.iclassq.service.impl;

import jakarta.validation.ValidationException;
import org.iclassq.entity.Frequency;
import org.iclassq.repository.FrequencyRepository;
import org.iclassq.service.FrequencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class FrequencyServiceImpl implements FrequencyService {
    private final Logger logger = Logger.getLogger(FrequencyServiceImpl.class.getName());

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Override
    public List<Frequency> findAll() {
        return frequencyRepository.findAll();
    }

    @Override
    public Frequency findById(Integer id) {
        return frequencyRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Frecuencia no encontrada"));
    }
}
