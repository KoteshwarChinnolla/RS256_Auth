package com.auth.jwtsecurity.repository;

import com.auth.jwtsecurity.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
