package com.kame.springboot.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kame.springboot.model.Department;

@Repository  // リポジトリもコンポーネントです   サービスクラスを作って、このリポジトリのBeanインスタンスをフィールドとして組み込んで使います
public interface DepartmentRepository extends JpaRepository<Department, String> {

	// インタフェースなので、抽象メソッドを宣言だけする
	 <Deprtment extends Department> Department saveAndFlush(Department department);

	 // PostgreSQL だと、order by departmentId を付けないと、順番が、更新されたのが一番最後の順になってします。
	 public List<Department> findByDepartmentIdIsNotNullOrderByDepartmentIdAsc();
	 
	 //  public Optional<Department> findById(String departmentId);	
	 //  List<Department> findAll();
	 //  void deleteById(String departmentId); 
	 //  void deleteById(Department department);  
		 
}
