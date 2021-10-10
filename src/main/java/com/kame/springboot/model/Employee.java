package com.kame.springboot.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
// javax.validation.constraints.NotEmpty  こちらを使う
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.lang.Nullable;

import com.kame.springboot.annotation.DayCheck;  

@Entity
@Table(name = "employee")
@DayCheck(hireDateProperty="hireDate", retirementDateProperty="retirementDate", message = "退社日は、入社日の後の日付にしてください")  // 相関チェック
public class Employee {
	
	@Id
	@Column(name = "employeeid")  // 全て小文字にしてください、PostgreSQL のカラム名は全て小文字じゃないとエラーになる
	private String employeeId;
		
	@Column(name = "name" )
	@NotEmpty(message="名前を入力してください")
	private String name;
	
	@Column(name = "age")
	@Min(0)
	@Max(110)
	private int age;
	
	@Column(name = "gender")
	@Min(value=1, message = "性別を選択してください")
	@Max(value=2, message = "性別を選択してください")
	private int gender; // 性別 1:男 2:女
	
	@Column(name = "photoid")  // 全て小文字のカラム名
	private int photoId;  // リレーションがあるカラム
	
	@Column(name = "zipnumber")  // 全て小文字のカラム名
	@NotEmpty(message="郵便番号を入力してください")
	@Pattern(regexp = "^[0-9]{3}-[0-9]{4}$", message = "郵便番号は半角数字000-0000 の形式で入力してください")
	private String zipNumber;
	
	@Column(name = "pref")
	@NotEmpty(message = "都道府県選択してください")
	private String pref;
	
	@Column(name = "address")
	@NotEmpty(message = "住所を入力してください")
	private String address;
	
	@Column(name = "departmentid") // 全て小文字のカラム名 @NotEmpty つけない 新規の時にはnullが送られるので
	private String departmentId; // リレーションのあるカラム
	
	// メッセージプロパティに追加する  typeMismatch.java.util.Date=yyyy/MM/dd形式で入力してください     これで TypeMismatchException発生した時に、出る長いエラーメッセージを、上書きして変更できる
	@DateTimeFormat(iso = ISO.DATE, fallbackPatterns = { "yyyy/MM/dd", "yyyy-MM-dd" })  //iso = ISO.DATE だと 最も一般的な ISO 日付形式 yyyy-MM-dd  たとえば、"2000-10-31"   fallbackPatterns に設定したものは、エラーにしないで、受け取ってくれる
	@Column(name = "hiredate") // 全て小文字のカラム名
	@NotNull(message="入社日を入力してください")  // 日付には、@NotNullを使う
	private Date hireDate; // java.util.Date
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "retirementdate", nullable = true) // 全て小文字のカラム名
	@Nullable  // org.springframework.langパッケージのアノテーション これよりも先に、相関関係のバリデーションの方が行われるらしい
	private Date retirementDate;  // java.util.Date 

	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE}) 
	Photo photo;  // @OneToOne  だからフィールド名は単数形に。アクセッサの ゲッター セッターも追加する
		
	@ManyToOne
	Department department;
	
	/**
	 * 引数なしのコンストラクタ.
	 */
	public Employee() {
		super();
	}

	/**
	 * 引数ありのコンストラクタ.
	 * @param employeeId
	 * @param name
	 * @param age
	 * @param gender
	 * @param photoId
	 * @param zipNumber
	 * @param pref
	 * @param address
	 * @param departmentId
	 * @param hireDate
	 * @param retirementDate
	 */
	public Employee(String employeeId, @NotEmpty String name, @Min(0) @Max(110) int age, @Min(1) @Max(2) int gender,
			int photoId, String zipNumber, String pref, String address, String departmentId, Date hireDate,
			Date retirementDate) {
		super();
		this.employeeId = employeeId;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.photoId = photoId;
		this.zipNumber = zipNumber;
		this.pref = pref;
		this.address = address;
		this.departmentId = departmentId;
		this.hireDate = hireDate;
		this.retirementDate = retirementDate;
//		this.department = department; // いらない
//		this.photo = photo;  // いらない
	}
	
	/**
	 * 住所を表示するfullAdressプロパティ CSVファイル出力の時使う.
	 * @return String
	 */
	public String getFullAddress() {
		return "〒" + this.zipNumber + this.pref + this.address;
	}
	
	/**
	 * 性別をint型からString型の表示にする CSVファイル出力の時に使う.
	 * @param gender
	 * @return str
	 */
	public String getStringGender(int gender) {
		String str = "";
		switch(gender) {
		case 1:
			str = "男";
			break;
		case 2:
			str = "女";
			break;
		}
		return str;
	}
	
	// アクセッサ
	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getPhotoId() {
		return photoId;
	}

	public void setPhotoId(int photoId) {
		this.photoId = photoId;
	}

	public String getZipNumber() {
		return zipNumber;
	}

	public void setZipNumber(String zipNumber) {
		this.zipNumber = zipNumber;
	}

	public String getPref() {
		return pref;
	}

	public void setPref(String pref) {
		this.pref = pref;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public Date getRetirementDate() {
		return retirementDate;
	}

	public void setRetirementDate(Date retirementDate) {
		this.retirementDate = retirementDate;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Photo getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}		
}
