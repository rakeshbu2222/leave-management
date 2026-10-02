package com.company.leave.repository;

import com.company.leave.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository (DAO layer). Spring Data JPA writes the implementation for us at runtime.
 * JpaRepository already gives: save, findById, findAll, deleteById, count ...
 * Methods like existsByEmail are "derived queries": Spring builds the SQL from the method name.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // SELECT count(*) > 0 FROM employees WHERE email = ?
    boolean existsByEmail(String email);
    
    // SELECT count(*) > 0 FROM employees WHERE email = ? AND id <> ?
    boolean existsByEmailAndIdNot(String email, Long id);
}
