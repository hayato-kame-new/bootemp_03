package com.kame.springboot.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kame.springboot.model.Department;
import com.kame.springboot.repositories.DepartmentRepository;

@Transactional // クラスに対して記述した設定はメソッドで記述された設定で上書きされる このクラスで @Transactionalをつけて、コントローラにはつけない
@Service // サービスもコンポーネントです
public class DepartmentService { // departmentテーブルは、リレーションの 主テーブル側 employeeテーブルが子テーブル側

	@Autowired // @Autowiredアノテーションをつけると、DepartmentRepositoryインタフェースを実装した内部クラスのインスタンスが自動生成されて、使えるようになる
	DepartmentRepository departmentRepository; // コンポーネントのBeanとしてインスタンス生成されたものが、フィールドメンバになる

	@PersistenceContext // EntityManagerのBeanを自動的に割り当てるためのもの サービスクラスにEntityManagerを用意して使う その他の場所(コントローラーなど）には書けません １箇所だけ
	private EntityManager entityManager;

	/**
	 * 使えないメソッド. リポジトリのメソッド自動生成の findAll() メソッドはidを使うときに使う.
	 * JpaRepositoryに辞書によってメソッドの自動生成機能で使える. departmentIdだと、メソッド自動生成できない.
	 * 
	 * @return List<Department>
	 */
//	@SuppressWarnings("unchecked")
//	public List<Department> findAllDepartmentData() { 
//		return departmentRepository.findAll();  
//	}

	/**
	 * レコードを全件取得する. order by departmentId をつけておくこと. つけないと更新したものが一番最後の順になってしまうので
	 * 
	 * @return List<Department>
	 */
	@SuppressWarnings("unchecked")
	public List<Department> findAllOrderByDepId() {
		return departmentRepository.findByDepartmentIdIsNotNullOrderByDepartmentIdAsc();
	}

	/**
	 * 部署IDを生成する.
	 * 
	 * @return departmentGeneratedId
	 */
	@Transactional(readOnly = false)  // サービスクラスのメソッドにつけてください コントローラではつけないこと@Transactionalネストさせないこと
	public String generatedId() {
		// JPAには Criteria APIという機能があり JPQLを使わずにメソッドチェーンによってデータベースアクセスができる
		String departmentGeneratedId = null;
		// インスタンスを生成して、インタフェース CriteriaBuilder型の変数に格納する
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		// 特定のエンティティにアクセスするために、引数にエンティティのクラスプロパティを指定する
		CriteriaQuery<Department> query = builder.createQuery(Department.class);
		// エンティティを絞り込むためのルート
		Root<Department> root = query.from(Department.class);
		// CriteriaQueryインスタンスのインスタンスメソッドselectを使って、全件Departmentエンティティを取得できる departmentIdの順番を 降順にして並べ替えする 
		query.select(root).orderBy(builder.desc(root.get("departmentId"))); // getメソッドで、指定のプロパティの値
		// selectで、departmentId順で降順に並び替えした全件のエンティティを取得してから、更に、絞り込む その結果をリストに格納
		List<Department> list = (List<Department>) entityManager.createQuery(query).setFirstResult(0).setMaxResults(1)
				.getResultList();
		// リストの要素が空だったら、まだ、登録されてるものがないことなので、最初の値として、"D01"を代入する
		if (list.size() == 0) {
			departmentGeneratedId = "D01";
		} else { // リストの要素があったら、１つだけ入ってるので取得する
			Department lastGetDepartment = list.get(0);
			String lastID = lastGetDepartment.getDepartmentId();
			// 切り取る 数値変換
			int n = Integer.parseInt(lastID.substring(1, 3)) + 1;
			String result = String.format("D%02d", n);
			departmentGeneratedId = result;
		}
		return departmentGeneratedId;
	}

	/**
	 * 部署新規作成.departmentnameカラムがユニークなので同じ名前を登録しようとすると例外が発生する.このメソッドでは発生した例外を呼び出し元へ投げる.
	 * ロールバックの注意点として、非検査例外(RuntimeException及びそのサブクラス)が発生した場合はロールバックされるが、検査例外(Exception及びそのサブクラスでRuntimeExceptionのサブクラスじゃないもの)が発生した場合はロールバックされずコミットされる
	 * RuntimeException以外の例外が発生した場合もロールバックしたいので @Transactional(rollbackFor =
	 * Exception.class)としてExceptionおよびExceptionを継承しているクラスがthrowされるとロールバックされるように設定します.
	 * rollbackFor=Exception.class  全ての例外が発生した場合、ロールバックさせる.  
	 * 呼び出し元つまりコントローラ のメソッドでtry-catchする ここで例外処理をしてはいけない コントローラのメソッドで例外処理をするために、コントローラ及びコントローラのリクエストハンドラには @Transactional
	 * をつけないこと.
	 * throws宣言が必要 呼び出しもとへ投げる throws DataIntegrityViolationException必要. 
	 * @Transactional(readOnly=false, rollbackFor=Exception.class) をつけること. 
	 * 
	 * @param department
	 * @return true 成功<br>false 失敗
	 * @throws DataIntegrityViolationException
	 * @throws ConstraintViolationException
	 * @throws PersistenceException
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean create(Department department) throws DataIntegrityViolationException , ConstraintViolationException, PersistenceException{
		Query query = entityManager.createNativeQuery("insert into department (departmentid, departmentname) values (?,?)");
		query.setParameter(1, department.getDepartmentId());
		query.setParameter(2, department.getDepartmentName());
		int result = query.executeUpdate();
		if(result != 1) {
			return false;
		}		
		return true;		
	}
	
	/**
	 * 部署更新.departmentnameカラムがユニークなので同じ名前を登録しようとすると例外が発生する.このメソッドでは発生した例外を呼び出し元へ投げる.
	 * ロールバックの注意点として、非検査例外(RuntimeException及びそのサブクラス)が発生した場合はロールバックされるが、検査例外(Exception及びそのサブクラスでRuntimeExceptionのサブクラスじゃないもの)が発生した場合はロールバックされずコミットされる
	 * RuntimeException以外の例外が発生した場合もロールバックしたいので @Transactional(rollbackFor =
	 * Exception.class)としてExceptionおよびExceptionを継承しているクラスがthrowされるとロールバックされるように設定します.
	 * rollbackFor=Exception.class  全ての例外が発生した場合、ロールバックさせる.  
	 * 呼び出し元つまりコントローラ のメソッドでtry-catchする ここで例外処理をしてはいけない コントローラのメソッドで例外処理をするために、コントローラ及びコントローラのリクエストハンドラには @Transactional
	 * をつけないこと.
	 * throws宣言が必要 呼び出しもとへ投げる throws DataIntegrityViolationException必要. 
	 * @Transactional(readOnly=false, rollbackFor=Exception.class) をつけること. 
	 * 
	 * @param department
	 * @return true 成功<br>false 失敗
	 * @throws DataIntegrityViolationException
	 * @throws ConstraintViolationException
	 * @throws PersistenceException
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean update(Department department) throws DataIntegrityViolationException , ConstraintViolationException,PersistenceException{
		Query query = entityManager.createNativeQuery("update department set departmentname = ? where departmentid = ? ");
		query.setParameter(1, department.getDepartmentName());
		query.setParameter(2, department.getDepartmentId());
		int result = query.executeUpdate();
		if(result != 1) {
			return false;
		}		
		return true;		
	}

	/**
	 * 実際使ってないメソッド id じゃなくて、 departmentId だから、メソッド自動生成機能は使えない.
	 * 
	 * @return Optional<Department>
	 */
//	public Optional<Department> findByIdDepartmentData(String departmentId){
//		return departmentRepository.findById(departmentId);
//	}

	/**
	 * 実際使ってないメソッド id じゃなくて、 departmentId だから、メソッド自動生成機能は使えない.
	 * 
	 * @return void
	 */
//	public void deleteByIdDepartmentData(String departmentId) {
//		departmentRepository.deleteById(departmentId);// 注意 戻り値はないので、 return 付けません
//	}

	/**
	 * 実際使ってないメソッド id じゃなくて、 departmentId だから、メソッド自動生成機能は使えない.
	 * 
	 * @return void
	 */
//	public void deleteByEntityObject(Department department) {
//				departmentRepository.deleteById(department);// 注意 戻り値はないので、 return 付けません。
//	}	
	
	/**
	 * 部署を削除する.
	 * ロールバックの注意点として、非検査例外(RuntimeException及びそのサブクラス)が発生した場合はロールバックされるが、検査例外(Exception及びそのサブクラスでRuntimeExceptionのサブクラスじゃないもの)が発生した場合はロールバックされずコミットされる
	 * RuntimeException以外の例外が発生した場合もロールバックしたいので @Transactional(rollbackFor =
	 * Exception.class)としてExceptionおよびExceptionを継承しているクラスがthrowされるとロールバックされるように設定します.
	 * 呼び出し元つまりコントローラ のメソッドでtry-catchする ここで例外処理をしてはいけない コントローラには @Transactional
	 * をつけないこと つけると、UnexpectedRollbackException発生する トランザクションをコミットしようとした結果、予期しないロールバックが発生した場合にスローされます
	 * 
	 * @Transactional(readOnly=false, rollbackFor=Exception.class) をつけること throws
	 *                                PersistenceException が必要
	 * 
	 * @param departmentId
	 * @return true 成功<br>false 失敗
	 * @throws PersistenceException
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class) // ここに@Transactionalをつけて、コントローラにはつけないでください
	public boolean delete(String departmentId) throws PersistenceException { // throwsして、呼び出しもとで
		Query query = entityManager.createQuery("delete from Department where departmentid = :departmentId");
		query.setParameter("departmentId", departmentId);
		int result = query.executeUpdate(); // 成功したデータ数が返る 戻り値 int型
		if (result != 1) { // 失敗
			return false;
		}
		return true;
	}
	
	/**
	 * 部署インスタンスを取得する
	 * 
	 * @param departmentId
	 * @return department
	 */
	public Department getByDepartmentId(String departmentId) {
		Query query = entityManager.createQuery("from Department where departmentid = :departmentId"); // JPQLクエリー文 カラム名は 全て小文字
		query.setParameter("departmentId", departmentId);
		Department department = (Department) query.getSingleResult(); // getSingleResult() インスタンスメソッドの戻り値はjava.lang.Object 一つの型のない結果を返す
		return department;
	}

	/**
	 * 部署インスタンスが要素になるリストを取得する
	 * 
	 * @param departmentName
	 * @return list
	 */
	public List<Department> findByDepName(String departmentName) {
		Query query = entityManager.createNativeQuery("select * from department where departmentname = ?");  // "select * from department where departmentname = :a"
		query.setParameter(1, departmentName);  // query.setParameter("a", departmentName);
		@SuppressWarnings("unchecked")
		List<Department> list = (List<Department>) query.getResultList(); // SELECTクエリーを実行し、問合せ結果を型のないリストとして返します キャスト必要
		return list; // もし、存在してないなら、空のリストを返します。
	}
}
