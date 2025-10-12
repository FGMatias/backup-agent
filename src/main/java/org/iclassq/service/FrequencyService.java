package org.iclassq.service;

import org.iclassq.entity.Frequency;

import java.util.List;

public interface FrequencyService {
    List<Frequency> findAll();
    Frequency findById(Integer id);
}
