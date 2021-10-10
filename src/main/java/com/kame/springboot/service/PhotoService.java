package com.kame.springboot.service;

import java.io.IOException;
import java.io.InputStream;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kame.springboot.component.LogicBean;
import com.kame.springboot.repositories.PhotoRepository;

@Service
@Transactional
public class PhotoService { // リレーションの 主テーブル側  子テーブルはemployeeです
	
	@Autowired
	PhotoRepository photorepository;
	
	// @PersistenceContextは一つしかつけれない もしもコントローラに付けたら、コントローラの方を削除しないといけない
	@PersistenceContext  // EntityManagerのBeanを自動的に割り当てるためのもの サービスクラスにEntityManagerを用意して使う。 その他の場所には書けません。１箇所だけ
	private EntityManager entityManager;
	
	@Autowired
	LogicBean logicBean;  // ロジッククラスのインスタンスをBeanとして組み込んでいる
	
	/**
	 * データを全て読み込んでbyte配列に格納して返す インスタンスメソッド
	 * コントローラクラスでは、このサービククラスのインスタンスメソッドとして呼び出して使う。
	 * 例外を投げる可能性があるの、Employeeコントローラクラスでtry-catchで囲む p193行目付近
	 * @param is
	 * @return byte[]
	 * @throws IOException
	 */
	 public byte[] readAll_fromService(InputStream is) throws IOException { // throws宣言してる
		 return logicBean.readAll(is);  // ロジッククラスのインスタンスメソッドを呼び出している
	 }
	 
	 /**
	  * photoテーブルに新規に登録する 主キーのカラムphotoidは、自動採番する  photoId serial primary key です
	  * @param photoData バイト配列
	  * @param mime コンテンツタイプ
	  * @return true:成功<br />false:失敗
	  */
	 public boolean photoDataAdd(byte[] photoData, String mime) {		 
		 // createNativeQuery を使う時には、PostgreSqlだと、 テーブル名も小文字です。createNativeQuery は、JPQLを使わないで普通のSQL文になる
		 Query query = entityManager.createNativeQuery("insert into photo ( photodata , mime )" +  " values ( :a, :b)");		 
		 query.setParameter("a", photoData);
		 query.setParameter("b", mime);
		 int result = query.executeUpdate(); // 戻り値は、データの更新や、削除に成功したエンティティの数です
		if (result != 1) { // 失敗
			return false;  // 失敗したら falseを返す
		}
		// 成功したら、
		return true;
	 }
	 
	 /**
	  * 新規登録の手順では、photoテーブル挿入の後にemployeeテーブルを挿入する.
	  * photoテーブル挿入は、すでに終わってるので、さきにphotoテーブルに新規挿入したphotoidを取ってきて、
	  * employeeテーブルのリレーションのカラムに設定する必要がある
	  * 現在photoテーブルに登録されているデータがあれば、photoidを最後のものを取ってくる
	  * まだ、photoテーブルに一件も登録されていなければ、規定値(デフォルト値)の 0 を返す(データベースで直接削除してたりしてたら、無いこともありうる)
	  * @return getPhotoId
	  */
	 public int getLastPhotoId() {
		 int getPhotoId = 0;
		 //  createNativeQueryの引数は、JPQLクエリーじゃない 普通のSQL文です PostgreSQL は、カラム名を全て小文字にしてください テーブル名も全て小文字です。photoid にすること
		 Query query = entityManager.createNativeQuery("select photoid from photo order by photoid desc limit 1");
		 try {
			 getPhotoId = (int)query.getSingleResult();			 
		 } catch (NoResultException e) {
			 e.printStackTrace();
			 return 0;  
		 }
		 return getPhotoId;  // 最後のphotoidの値を取得して返す
	 }

	 /**
	  * 社員のフォームの場所に写真を表示するために バイトデータを取得する PhotoDisplayControllerで使用する
	  * 社員新規作成の時は、まだ表示する画像は無いはず、(直接データベースに登録していれば別) photoId が 0 の時は メソッドは呼ばないようにしてる
	  * 呼び出しもと(PhotoDisplayController)で  if (photoId != 0) {}の条件をつけて photoId が 0 の時は メソッドは呼ばないようにしてる
	  * @param photoId
	  * @return byteData
	  */
	 public byte[] getPhotoData(int photoId) {		 
		 byte[] byteData = null;		 
		 Query query = entityManager.createNativeQuery("select photodata from photo where photoid = ?");
		 query.setParameter(1, photoId);		 
		 byteData = (byte[]) query.getSingleResult(); 
		 return byteData; 
	 }
	 
	 /**
	  * コンテンツタイプの取得 PhotoDisplayControllerで使用する
	  * 呼び出しもと(PhotoDisplayController)で  if (photoId != 0) {}の条件をつけて photoId が 0 の時は メソッドは呼ばないようにしてる
	  * @param photoId
	  * @return mime
	  */
	public String getMime(int photoId) {
		String mime = "";
		Query query = entityManager.createNativeQuery("select mime from photo where photoid = ?");
		 query.setParameter(1, photoId);
		 mime = (String) query.getSingleResult();		
		return mime;
	}
	
	/**
	 * photoDataを更新
	 * @param photoId
	 * @param photoData
	 * @param mime
	 * @return true:成功<br />false:失敗
	 */
	public boolean photoDataUpdate(int photoId, byte[] photoData, String mime) {
		Query query = entityManager.createNativeQuery("update photo set photodata = ?, mime = ? where photoid = ?");  // カラムは全て小文字にすること
		query.setParameter(1, photoData).setParameter(2, mime).setParameter(3, photoId);
		// これでもいい
		// Query query = entityManager.createNativeQuery("update photo set photodata = :a, mime = :b where photoid = :c");  // カラムは全て小文字にすること
		// query.setParameter("a", photoData).setParameter("b", mime).setParameter("c", photoId);
		int result = query.executeUpdate(); // 更新成功したデータ数が返る photoidが ユニーク(一意)なので、一つだけ更新する
		if(result != 1) { // 失敗
			return false;  // 失敗したら、 呼び出しもとに falseを返す  return で、メソッドを即終了して、引数を呼び出しもとに返すので、下の行は実行されない
		}
		return true; // 成功したら、ここまできたら成功だから、 trueを返す
	}

}
