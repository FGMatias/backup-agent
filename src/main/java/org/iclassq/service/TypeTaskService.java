package org.iclassq.service;

import org.iclassq.entity.TypeTask;

import java.util.List;

public interface TypeTaskService {
    List<TypeTask> findAll();
    TypeTask findById(Integer id);
}
