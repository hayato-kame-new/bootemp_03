package com.kame.springboot.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name="department") // postgresだと、全て小文字なので小文字にする
public class Department {
	
	@Id
	@Column(name = "departmentid") // カラム名は、postgreSQL 全て小文字なので小文字にする
	private String departmentId;  // 新規の時には、nullで渡っていくので、バリデーションを@NotEmpty を付けない  リレーションのカラム
	
	// departmentテーブルの departmentnameカラムには 一意をつける
	// ALTER TABLE department ADD CONSTRAINT constraint_name UNIQUE (departmentName);
	// @UniqueDepName   //  自作したアノテーション 部署名はユニークでなければいけない これも使えます。 こっちを使わない時には、例外処理を使っています。
	@NotEmpty  // String型にはこれを使う。@NotNullだと、String型には効かない unique = true つけて、データベースのテーブルのカラムにも一意制約つける
	@Column(name = "departmentname", length = 20, nullable = false, unique = true) // カラム名は、postgreSQL の全て小文字に合わせる unique = true をつけて、テーブル定義でも、最初にUNIQUEをつけておくこと
	private String departmentName;
	
	@OneToMany(mappedBy = "departmentId", cascade = CascadeType.ALL)
	List<Employee> employees;
	
	// 引数なしコンストラクタ
	public Department() {
		super();
	}
	
	// 引数ありコンストラクタ
	public Department(String departmentId, @NotEmpty String departmentName) {
		super();
		this.departmentId = departmentId;
		this.departmentName = departmentName;
	}

	// アクセッサ
	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	// リレーションのゲッター
	public List<Employee> getEmployees() {
		return employees;
	}
	// リレーションのセッター
	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}
}
