package com.kame.springboot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kame.springboot.model.Employee;

@Repository // リポジトリもコンポーネントです
public interface EmployeeRepository extends JpaRepository<Employee, String> { 

	// public List<Employee> findByEmployeeIdIsNotNullOrderByEmployeeIdAsc();
}
