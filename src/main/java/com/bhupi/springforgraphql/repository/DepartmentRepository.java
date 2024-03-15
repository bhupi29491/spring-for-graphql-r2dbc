package com.bhupi.springforgraphql.repository;

import com.bhupi.springforgraphql.model.Department;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends ReactiveCrudRepository<Department, Integer> {
}
