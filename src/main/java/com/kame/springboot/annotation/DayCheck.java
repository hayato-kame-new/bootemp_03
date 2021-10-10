package com.kame.springboot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.kame.springboot.validator.DayCheckValidator;
// アノテーションクラス
@Documented
@Constraint(validatedBy = {DayCheckValidator.class}) // バリデータクラスを指定する。
// @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DayCheck {
	
	// String message() default "退社日は入社日より後の日付を入力してください";

	  Class<?>[] groups() default {};

	  Class<? extends Payload>[] payload() default {};
	  
	  // つけたし相関チェック  @Targetに、「TYPE」を指定します  相関チェックの場合は項目にアノテーションはつけられません
	  // 単一の項目と違い、相関チェックの場合は項目にアノテーションをつけることができません。ひとつしか、項目がとれませんから

	  // だから、アノテーションをFormクラスにつける必要があり、そのためには@TargetはTYPEにしないといけないわけです。
	  // あと、単項目と違ってアノテーションが受け取るオブジェクトがFormクラスそのものになるので、実装クラスではFormクラス内の各項目を参照することになります。
	  // Formクラスの宣言の上に  @DayCheck(hireDateProperty="hireDate", retirementDateProperty="retirementDate", message = "退社日は、入社日の後の日付にしてください")  をつけてください
	  
	  // インタフェースにその項目を取得するためのメソッド定義だけしておく
	  String hireDateProperty();
      String retirementDateProperty();
      String message();

      @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
      @Retention(RetentionPolicy.RUNTIME)
      @Documented
      public @interface List {
    	  DayCheck[] value();
       }
}
