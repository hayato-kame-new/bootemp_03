package com.kame.springboot.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.kame.springboot.component.LogicBean;
import com.kame.springboot.model.Employee;
import com.kame.springboot.repositories.EmployeeRepository;

@Service
@Transactional //  クラスに対して記述した設定はメソッドで記述された設定で上書きされる このクラスで @Transactionalをつけて、コントローラにはつけない
public class EmployeeService {  // リレーションの従テーブル 

	@Autowired
	EmployeeRepository employeeRepository;

	// @PersistenceContextは一つしかつけれない コントローラなどの方につけてたら削除する
	@PersistenceContext // EntityManagerのBeanを自動的に割り当てるためのもの サービスクラスにEntityManagerを用意して使う。その他の場所には書けません。１箇所だけ
	private EntityManager entityManager;

	@Autowired
	LogicBean logicBean;

	/**
	 * レコードを全件取得する.こっちはエラー出るので使わない.  idが、employeeIdのため メソッドの自動生成使えない.
	 * リポジトリのメソッドの自動生成機能で作るfindAll()メソッドも使わない.
	 * @return List<Department>
	 */
//	@SuppressWarnings("unchecked")
//	public List<Employee> findAllOrderByEmpId() { 
//		return employeeRepository.findByEmployeeIdIsNotNullOrderByEmployeeIdAsc();
//	}

	/**
	 * レコードを全件取得する.こっちを使う 
	 * order by employeeid を付けないと 順番が更新されたのが一番最後の順になってしまうのでorder byをつける.
	 * @return list
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getEmpListOrderByAsc() {
		Query query = entityManager.createNativeQuery("select * from employee  order by employeeid asc"); //  order by employeeid が必要です employeeidは小文字
		List<Employee> list = query.getResultList();
		return list;
	}

	/**
	 * エンティティ取得する リポジトリの自動生成機能で作るfindByIdを使えないid でなく、employeeId なので.(findByIdは、引数はidのほか エンティティも引数にできる)
	 * 
	 * @param employeeId
	 * @return employee
	 */
	public Employee getEmp(String employeeId) {
		Query query = entityManager.createNativeQuery("select * from employee where employeeid = ?");  // employeeidは全て小文字
		query.setParameter(1, employeeId);
		List<Employee> list = (List<Employee>) query.getResultList();
		Iterator itr = list.iterator();
		Employee emp = new Employee();
		List<Employee> resultlist = new ArrayList<Employee>(); // newして確保

		while (itr.hasNext()) {
			Object[] obj = (Object[]) itr.next();
			// String employeeId = String.valueOf(obj[0]);
			String name = String.valueOf(obj[1]);
			int age = Integer.parseInt(String.valueOf(obj[2]));
			int gender = Integer.parseInt(String.valueOf(obj[3]));
			int photoId = Integer.parseInt(String.valueOf(obj[4]));
			String zipNumber = String.valueOf(obj[5]);
			String pref = String.valueOf(obj[6]);
			String address = String.valueOf(obj[7]);
			String departmentId = String.valueOf(obj[8]);

			java.sql.Date sqlHireDate = (Date) obj[9]; // 1999-11-11

			java.util.Date utilHireDate = new Date(sqlHireDate.getTime()); // 1998-12-12

			// 退職日は、nullかもしれないので、java.sql.Date でnullだったら、java.util.Date でもnullのまま
			java.util.Date utilRretirementDate = null;

			if (obj[10] != null) {
				java.sql.Date sqlRretirementDate = (Date) obj[10]; // 2003-03-03

				long longDate = sqlRretirementDate.getTime();
				utilRretirementDate = new java.util.Date(longDate);// Mon Mar 03 00:00:00 JST 2003
			}
			// セットしていく
			emp.setEmployeeId(employeeId);
			emp.setName(name);
			emp.setAge(age);
			emp.setGender(gender);
			emp.setPhotoId(photoId);
			emp.setZipNumber(zipNumber);
			emp.setPref(pref);
			emp.setAddress(address);
			emp.setDepartmentId(departmentId);
			emp.setHireDate(utilHireDate);
			emp.setRetirementDate(utilRretirementDate);

			// 引数ありコンストラクタを使うと
			// Employee emp = new Employee(id, name,
			// age,gender,photoId,zipNumber,pref,address,departmentId,utilHireDate,
			// utilRretirementDate);
			resultlist.add(emp);
		}
		Employee employee = resultlist.get(0);
		return employee;
	}

//	public String generateEmpIdFromCriteria() {
//		String generatedEmpId = null;
//		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
//		CriteriaQuery<Employee> query = builder.createQuery(Employee.class);
//		Root<Employee> root = query.from(Employee.class);
//		query.select(root).orderBy(builder.desc(root.get("employeeId")));
//		List<Employee> list = (List<Employee>) entityManager.createQuery(query).setFirstResult(0).setMaxResults(1)
//				.getResultList();
//		// 社員IDが 辞書順に並び替えて、最後にあるものを取得した。それがリストに１つ入ってるはず、リストに要素が一つも無かったら、まだ、全く登録されていないので
//		if (list.size() == 0) {
//			generatedEmpId = "EMP0001"; // 一番最初になります。まだ、ひとつも、employeeデータが登録されてなかったら
//		} else {
//			Employee employee = list.get(0);
//			String getLastId = employee.getEmployeeId(); // 最後に登録されているId
//			// 文字列切り取りして、数値に変換して、 +1 する それをまた、文字列にフォーマットで変換する
//			generatedEmpId = String.format("EMP%04d", Integer.parseInt(getLastId.substring(3)) + 1);
//		}
//		return generatedEmpId;
//	}
		
	/**
	 * 社員IDを生成する
	 * @return getGeneratedEmpId
	 */
	public String generateEmpId() {
		// まず、最後尾の社員IDをとってくる order by 辞書順で並べ替えて、desc をして limit 1
		// createNativeQuery メソッドは、JPQLではなくて普通のSQL文です employeeid カラムは、全てを小文字にすること postgreSQLだから テーブル名 カラム名 全て小文字
		Query query = entityManager
				.createNativeQuery("select employeeid from employee order by employeeid desc limit 1");
		String lastStringEmpId = (String) query.getSingleResult();// 戻り値は、型のないObjectになるので、キャストする
		int plusOne = Integer.parseInt(lastStringEmpId.substring(3)) + 1;
		String getGeneratedEmpId = String.format("EMP%04d", plusOne);
		return getGeneratedEmpId;
	}

	/**
	 * 社員新規登録
	 * @param employee
	 * @return true 成功<br>false 失敗
	 */
	public boolean empAdd(Employee employee) {
		Query query = entityManager.createNativeQuery(
				"insert into employee (employeeid, name, age, gender, photoid, zipnumber, pref, address, departmentid, hiredate, retirementdate) values (?,?,?,?,?,?,?,?,?,?,?)");
		query.setParameter(1, employee.getEmployeeId());
		query.setParameter(2, employee.getName());
		query.setParameter(3, employee.getAge());
		query.setParameter(4, employee.getGender());
		query.setParameter(5, employee.getPhotoId());
		query.setParameter(6, employee.getZipNumber());
		query.setParameter(7, employee.getPref());
		query.setParameter(8, employee.getAddress());
		query.setParameter(9, employee.getDepartmentId());

		// データベースに保存する時には、java.util.Date から java.sql.Date に変換すること
		// データベース型に対応するTemporalType列挙型に設定する属性です。指定できる値は，次の3種類です。
		// TemporalType.DATE：java.sql.Dataと同じです。TemporalType.TIME：java.sql.Timeと同じです。
		// TemporalType.TIMESTAMP：java.sql.Timestampと同じです。
		// 入社日は、必ず入力してもらってるので、nullではない
		query.setParameter(10, new java.sql.Date(employee.getHireDate().getTime()), TemporalType.DATE);   // TemporalType.DATE は java.sql.Dataで登録するという意味

		// 退職日は、null回避しないといけない
		java.util.Date utilRetireDate = employee.getRetirementDate(); // 未入力のとき null 入ってる
		java.sql.Date sqlRetireDate = null;
		if (employee.getRetirementDate() != null) {
			long retireLong = utilRetireDate.getTime(); // nullの時に変換しようとするとエラーここで発生するので
			sqlRetireDate = new java.sql.Date(retireLong);
		}
		query.setParameter(11, sqlRetireDate, TemporalType.DATE);

		int result = query.executeUpdate(); // 成功したデータ数が返る
		if (result != 1) {
			// 失敗
			return false;
		}
		return true;
	}

	/**
	 * エラーメッセージを取得する 全部.使ってはいない
	 * @param result
	 * @return errorMessages
	 */
	public String addAllErrors(BindingResult result) {
		String errorMessages = "";
		for (ObjectError error : result.getAllErrors()) {
			// ここでメッセージを取得する。
			errorMessages += error.getDefaultMessage();
		}
		return errorMessages;
	}

	/**
	 * アノテーションなしに行ったすべてのエラーをBindingResultに追加する.使ってはいない
	 * @param bindingResult
	 * @param errorMap
	 * @return true 成功<br>false 失敗
	 */
	public static BindingResult addAllErrors(BindingResult bindingResult, Map<String, String> errorMap) {
		for (Map.Entry<String, String> entry : errorMap.entrySet()) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), entry.getKey(), entry.getValue());
			bindingResult.addError(fieldError);
		}
		return bindingResult;
	}

	/**
	 * 社員更新
	 * @param employee
	 * @return true 成功<br>false 失敗
	 */
	public boolean empUpdate(Employee employee) {
		Query query = entityManager.createNativeQuery(
				"update employee set (name, age, gender, photoid, zipnumber, pref, address, departmentid, hiredate, retirementdate) = (?,?,?,?,?,?,?,?,?,?) where employeeid = ? ");
		query.setParameter(1, employee.getName());
		query.setParameter(2, employee.getAge());
		query.setParameter(3, employee.getGender());
		query.setParameter(4, employee.getPhotoId());
		query.setParameter(5, employee.getZipNumber());
		query.setParameter(6, employee.getPref());
		query.setParameter(7, employee.getAddress());
		query.setParameter(8, employee.getDepartmentId());
		// データベースに保存する時には、java.util.Date から java.sql.Date に変換すること 入社日は、nullは無いので変換でエラーが出ない
		// データベースに保存する時には、java.util.Date から java.sql.Date に変換すること
		// データベース型に対応するTemporalType列挙型に設定する属性です。指定できる値は，次の3種類です。
		// TemporalType.DATE：java.sql.Dataと同じです。TemporalType.TIME：java.sql.Timeと同じです。
		// TemporalType.TIMESTAMP：java.sql.Timestampと同じです。
		query.setParameter(9, new java.sql.Date(employee.getHireDate().getTime()), TemporalType.DATE); // TemporalType.DATE は java.sql.Dataで登録するという意味

		// 退職日は、null回避しないといけない
		java.util.Date utilRetireDate = employee.getRetirementDate(); // 未入力のとき null 入ってる
		java.sql.Date sqlRetireDate = null;
		if (employee.getRetirementDate() != null) { // null じゃなければ変換する
			long retireLong = utilRetireDate.getTime(); // nullの時に変換しようとするとエラーここで発生するので
			sqlRetireDate = new java.sql.Date(retireLong);
		}
		query.setParameter(10, sqlRetireDate, TemporalType.DATE);  //  TemporalType.DATE は java.sql.Dataで登録するという意味

		query.setParameter(11, employee.getEmployeeId());

		int result = query.executeUpdate();
		if (result != 1) { // 失敗
			return false; // 失敗したら false が返る
		}
		return true;
	}

	/**
	 * 社員削除
	 * @param employeeId
	 * @return true 成功<br>false 失敗
	 */
	public boolean deleteEmployee(String employeeId) {
		Query query = entityManager.createNativeQuery("delete from employee where employeeid = ?");
		query.setParameter(1, employeeId);
		int result = query.executeUpdate();
		if (result != 1) { // 失敗
			return false; // 失敗したら false が返る
		}
		return true;
	}

	/**
	 * 社員検索
	 * @param departmentId
	 * @param employeeId
	 * @param word
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> find(String departmentId, String employeeId, String word) {
		// 注意 引数のdepartmentId は 空文字とnullの可能ある   employeeId と word は ""空文字の可能性ある

		String sql = "select * from employee";
		String where = ""; // where句
		int depIdIndex = 0; // プレースホルダーの位置を指定する 0だと、プレースホルダーは使用しないことになる
		int empIdIndex = 0;
		int wordIndex = 0;

		if (departmentId == null) {
			departmentId = "";
		}
		if (departmentId.equals("")) {
			// 未指定の時 何もしない depIdIndex 0 のまま変更無し
		} else {
			where = " where departmentid = ?"; // 代入する 注意カラム名を全て小文字にすること departmentid また、前後半角空白入れてつなぐので注意
			depIdIndex = 1; // 変更あり
		}

		if (employeeId.equals("")) {
			// 未指定の時 何もしない 
		} else {
			if (where.equals("")) { 
				where = " where employeeid = ?"; // 代入する カラム名を全て小文字 employeeid
				empIdIndex = 1;
			} else {
				where += " and employeeid = ?"; // where句はすでにあるので 二項演算子の加算代入演算子を使って連結 												
				empIdIndex = depIdIndex + 1;
			}
		}

		if (word.equals("")) {
			// 未指定の時何もしない
		} else {
			if (where.equals("")) { 
				where = " where name like ?"; // 代入  
				 wordIndex = 1;
			} else if (where.equals(" where departmentid = ?")) {
				where += " and name like ?"; // 二項演算子の加算代入演算子を使って連結 
				 wordIndex = depIdIndex + 1;
			} else if (where.equals(" where employeeid = ?")) {
				where += " and name like ?"; // 二項演算子の加算代入演算子を使って連結 
				 wordIndex = empIdIndex + 1;
			} else if (where.equals(" where departmentid = ? and employeeid = ?")) {
				where += " and name like ?"; // 二項演算子の加算代入演算子を使って連結 
				 wordIndex = depIdIndex + empIdIndex + 1;
			}
		}

		Query query = entityManager.createNativeQuery(sql + where);
		if (depIdIndex > 0) {
			query.setParameter(depIdIndex, departmentId);
		}
		if (empIdIndex > 0) {
			query.setParameter(empIdIndex, employeeId);
		}
		if (wordIndex > 0) {
			query.setParameter(wordIndex, "%" + word + "%");
		}
		return query.getResultList(); // 結果リスト 型のないリストを返す 
	}

	// 検索こっちは使わない
//	@SuppressWarnings("unchecked")
//	public List<Employee> find2(String departmentId, String employeeId, String word) {
//		// 注意 引数のdepartmentId 空文字とnullの可能ある employeeId word ""空文字の可能性ある
//		List<Employee> list = new ArrayList<Employee>();
//
//		String sql = "select * from employee";
//		String where = ""; // where句
//		int depIdIndex = 0; // プレースホルダーの位置を指定する 0だと、プレースホルダーは使用しないことになる
//		int empIdIndex = 0;
//		int wordIndex = 0;
//
//		if (departmentId == null) {
//			departmentId = "";
//		}
//		if (departmentId.equals("")) {
//			// 未指定の時 何もしない depIdIndex 0 のまま変更無し
//		} else {
//			where = " where departmentid = ?"; // 代入する 注意カラム名を全て小文字にすること departmentid また、前後半角空白入れてつなぐので注意
//			depIdIndex = 1; // 変更あり
//		}
//		// この時点では、 depIndex は、 0 か 1 になってる
//
//		if (employeeId.equals("")) {
//			// 未指定の時 何もしない depIdIndex 0 か 1 empIdIndex 0 のまま変更無し
//		} else {
//			if (where.equals("")) { // 入力があり、かつ、where句が 空文字の時(depIdIndex 0) この時、empIdIndex は 1 に変更
//				where = " where employeeid = ?"; // 代入する カラム名を全て小文字 employeeid
//			} else {
//				where += " and employeeid = ?"; // where句はすでにあるので(depIdIndex 1) 二項演算子の加算代入演算子を使って連結 この時 empIdIndex は 2
//												// に変更
//			}
//			empIdIndex = depIdIndex + 1;
//		}
//		// この時点では、 depIndex は、 0 か 1 になってる empIdIndex は、 0 か 1 か 2 になってる
//
//		if (word.equals("")) {
//			// 未指定の時何もしない、 depIdIndex 0 か 1 empIdIndex 0 か 1 か 2 のまま変更無し
//		} else {
//			if (where.equals("")) { // 入力があり、かつ、where句が 空文字の時(depIdIndex 0 empIdIndex 0) この時 wordIndex は 1 に変更
//				where = " where name like ?"; // 代入
//			} else if (where.equals(" where departmentid = ?")) {
//				where += " and name like ?"; // 二項演算子の加算代入演算子を使って連結
//			} else if (where.equals(" where employeeid = ?")) {
//				where += " and name like ?"; // 二項演算子の加算代入演算子を使って連結
//			} else if (where.equals(" where departmentid = ? and employeeid = ?")) {
//				where += " and name like ?"; // 二項演算子の加算代入演算子を使って連結
//			}
//			wordIndex = depIdIndex + empIdIndex + 1;
//		}
//
//		Query query = entityManager.createNativeQuery(sql + where);
//		if (depIdIndex > 0) {
//			query.setParameter(depIdIndex, departmentId);
//		}
//		if (empIdIndex > 0) {
//			query.setParameter(empIdIndex, employeeId);
//		}
//		if (wordIndex > 0) {
//			query.setParameter(wordIndex, "%" + word + "%");
//		}
//		List resultRist = query.getResultList(); // 結果リスト 型のないリストを返す
//		// list はArrayList<E> です。中には要素としてObject型の配列オブジェクトObject[11]が入ってます。
//		Employee employee = new Employee();
//		Iterator itr = resultRist.iterator();
//		while (itr.hasNext()) {
//			Object[] object = (Object[]) itr.next(); // next() 反復処理で次の要素を返します。
//			// [EMP0001, 山田 太郎, 100, 1, 1, 111-1111, 千葉県, 千代田区, D01, 2000-12-10, 2000-12-12]
//			// Object型の配列オブジェクトObject[11] は、要素が11個あるので、先頭の要素から取得していく
//
//			String findResultEmpId = (String) object[0]; // 左辺を String.valueOf(obj[0]); にしてもいい
//			String name = (String) object[1]; // 左辺を String.valueOf(obj[1]); にしてもいい
//			int age = (int) object[2]; // int age = Integer.parseInt(String.valueOf(obj[2]));
//			int gender = (int) object[3]; // int gender = Integer.parseInt(String.valueOf(obj[3]));
//			int photoId = (int) object[4]; // int photoId = Integer.parseInt(String.valueOf(obj[4]));
//			String zipNumber = (String) object[5];// String.valueOf(obj[5]);
//			String pref = (String) object[6];// String.valueOf(obj[6]);
//			String address = (String) object[7];// String.valueOf(obj[7]);
//			String findResultDepartmentId = (String) object[8];// String.valueOf(obj[8]);
//			// 入社日
//			// 下でもOK
//			// java.sql.Date sqlHireDate = (Date)object[9]; // 左辺を (new
//			// SimpleDateFormat("yyyy-MM-dd")).format(object[9]); でもいける
//			// java.util.Date utilHireDate = new java.util.Date(sqlHireDate.getTime());
//			String str = (new SimpleDateFormat("yyyy-MM-dd")).format(object[9]);
//			java.util.Date date = null;
//			try {
//				date = (new SimpleDateFormat("yyyy-MM-dd")).parse(str);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//			// 退社日
//			java.sql.Date sqlRetirementDate = null;
//			java.util.Date utilRetirementDate = null;
//			if (object[10] != null) {
//				sqlRetirementDate = (Date) object[10];
//				utilRetirementDate = new java.util.Date(sqlRetirementDate.getTime());
//			}
//			// employeeインスタンスの各フィールドへセットする
//			employee.setEmployeeId(findResultEmpId); // 引数で渡ってきた employeeIdは使わないこと
//			employee.setName(name);
//			employee.setAge(age);
//			employee.setGender(gender);
//			employee.setPhotoId(photoId);
//			employee.setZipNumber(zipNumber);
//			employee.setPref(pref);
//			employee.setAddress(address);
//			employee.setDepartmentId(findResultDepartmentId); // 引数で渡ってきた departmentIdは使わないこと
//			employee.setHireDate(date);
//			// employee.setHireDate(utilHireDate);
//			employee.setRetirementDate(utilRetirementDate);
//
//			// 引数ありコンストラクタを使うと
//			// Employee emp = new Employee(findResultEmpId, name, age, gender, photoId,
//			// zipNumber, pref, address, findResultDepartmentId, utilHireDate,
//			// utilRretirementDate);
//			list.add(employee);
//		}
//		return list; // 見つからない時は、空のリストが返る
//	}
}
