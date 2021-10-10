package com.kame.springboot.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.kame.springboot.annotation.UniqueDepName;
import com.kame.springboot.service.DepartmentService;


public class DepartmentNameValidator implements ConstraintValidator<UniqueDepName, String> {

	@Autowired
	DepartmentService departmentService;

	@Override
	public void initialize(UniqueDepName constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	
	//NULLチェックを行い、NULLだった場合はtrueを返してください。それが決まりです。なぜなら、NULLチェックは@NotNullを併用して行うことになっているからです。Stringだと@NotEmptyだけど

	// 引数は、部署名のフォームで入力した文字列
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(value == null || value.isEmpty()) { //  nullチェックを前に書いて先に評価する それから""空文字チェックを評価すること
			return false;
		}
		List list = departmentService.findByDepName(value); 
		// リストに要素があったら、すでに存在している部署名と同じ名前で登録しようとしているので、falseを返して、バリデーションエラーを発生させる
		if(list.size() == 0) {
			return true;  //  空のリストだったら、存在してませんので true を返して、スルー
		}
		return false; // バリデーションエラーを出す
	}

}
