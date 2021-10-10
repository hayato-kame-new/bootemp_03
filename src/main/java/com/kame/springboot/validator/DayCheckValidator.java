package com.kame.springboot.validator;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.kame.springboot.annotation.DayCheck;

/**
 * 日付のパラメータの比較を行うバリデータクラス.2つのパラメータをとる 相関チェック.
 * ConstraintValidatorの<>の中には、<アノテーションクラス, アノテーションを付与する（チェックを行う）エンティティクラス> を指定する.
 * ConstraintValidator<DayCheck, Object>  にする.Employeeを<>に書かないで 汎用的にするには Objectにする.
 * @author skame
 *
 */
public class DayCheckValidator implements ConstraintValidator<DayCheck, Object> {

	// フィールド     アノテーションクラスのメソッドと、名前を合わせたフィールド名にするといい
	String hireDateProperty; // 入力された値 日付文字列
    String retirementDateProperty;  // 入力された値 日付文字列
    String message;
	
	/**
	 * 初期化処理
	 */
	@Override
	public void initialize(DayCheck constraintAnnotation) {
		// アノテーションの引数情報を設定する。
		this.hireDateProperty = constraintAnnotation.hireDateProperty();
		this.retirementDateProperty = constraintAnnotation.retirementDateProperty();
		this.message = constraintAnnotation.message();
	}

	/**
	 * 比較処理
	 * false を返すとバリデーションエラーを発生
	 */
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		
		// valueは、フォームのオブジェクト Formクラスのオブジェクト
		 boolean result = true;
	      if( value == null){ 
	             result = true; // ここでメソッドの終了 呼び出しもとへtrueを返す
	      }else{
	    	  // フォームクラスから比較対象項目の値を得る SpringのBeanWrapperというインタフェースを使います
	    	  BeanWrapper beanWrapper = new BeanWrapperImpl(value);
	    	  
	    	  Date date1 =  (Date) beanWrapper.getPropertyValue(hireDateProperty);  // 未入力だと null
	    	  Date date2 =  (Date) beanWrapper.getPropertyValue(retirementDateProperty);  // 未入力だと null
	    	  
	    	  if (date1 == null || date2 == null) {
	    		  return true;  // ここでメソッドの終了 引数をメソッドの呼び出しもとに返す returnキーワード      true を返せば、バリデーションエラーにならない	    		  
	    	  }

	    	  // 退職日を前にしているので、退職日 >= 入社日  の時は、0以上の数値が返り、そうでないときはマイナス数値が返る	    	  
	    	  if(date2.compareTo(date1) >= 0) { 
	    		  result = true;  // ここでメソッドの終了 引数をメソッドの呼び出しもとに返す returnキーワード      true を返せば、バリデーションエラーにならない
	    	  } else {
	    		  // エラーメッセージを出します。エラーメッセージを生成する
	    		  //メッセージを設定する。エラーメッセージを返すときの手続き
	    	        context.disableDefaultConstraintViolation();  // まず、デフォルトの制約違反情報をクリアします
	    	        // 今回は退社日の下にエラーメッセージを表示したいので  addPropertyNode(retirementDateProperty) で、Formクラスの　退職日の名前をセットしてます
	    	        context.buildConstraintViolationWithTemplate(message).addPropertyNode(this.retirementDateProperty).addConstraintViolation();
	    		  result = false;  // エラーメッセージを出す。
	    	  }
	     }
	      return result;
		// 相関チェックのアノテーションは、Formクラスのクラス定義の部分に付与する  引数として項目名を渡す  各項目にはつけません
		

	      
	 // 単数チェックの時     
//		// 入社日は、null許可しない。必須。そもそも nullだと、バリデーションの@NotNullでチェックされるので、いらないとかもしれないが、先にどちらが適応されるかわからないので一応つける。
//		if (value.getHireDate() == null) {
//			return true;
//		}
//		// 退社日は、null許可する  先に退社日の nullをチェックする  日付がnullだとぬるぽが発生するのでnullチェック
//		if(value.getHireDate() != null && value.getRetirementDate() == null) {
//			return true;
//		}
//		// 入社日が退社日よりも前なら、true      後だったら falseを 返す falseを返したら、バリデーションエラーメッセージが出る
//		return value.getHireDate().before(value.getRetirementDate()) ? true : false; 
	}

}
