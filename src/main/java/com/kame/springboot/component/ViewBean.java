package com.kame.springboot.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kame.springboot.model.Department;
import com.kame.springboot.service.DepartmentService;

@Component  // コンポーネントにする。Bean化して使うクラスになる
public class ViewBean {  

	// フィールド宣言
	@Autowired
	DepartmentService departmentService;

	@Autowired  // このクラスを コンポーネントにするには、 コンストラクタに、@Autowired　を付けます
	public ViewBean() {
		super();
	}
	
	/**
	 * 性別のラジオボタンのためのMapを作って返す. 
	 * @return Map<Integer, String>
	 */
	public Map<Integer, String> getGenderMap() {
		Map<Integer, String> genderMap = new LinkedHashMap<Integer, String>();  // LinkedHashMap  は追加された順番を保持する
		genderMap.put(1, "男性");
		genderMap.put(2, "女性");
		return genderMap;
	}
	
	/**
	 * selectタグのドロップダウンリストの都道府県のMapを作って返す.
	 * @return Map<Integer, String>
	 */
	public Map<Integer, String> getPrefMap() {
		 Map<Integer, String> prefMap = new LinkedHashMap<Integer, String>();
		List<String> list = new ArrayList<String>(Arrays.asList("東京都", "神奈川県", "埼玉県", "千葉県", "茨城県")); // LinkedHashMap  は追加された順番を保持する
		for(int i = 0; i < list.size(); i++) {
			prefMap.put(i, list.get(i));
		}
		return prefMap;
	}
	
	/**
	 * selectタグのドロップダウンリストの部署のMapを作って返す.
	 * @return Map<Integer, String>
	 */
	public Map<String, String> getDepartmentMap() {
		 Map<String, String> depMap = new LinkedHashMap<String, String>();  // LinkedHashMap  は追加された順番を保持する
		 List<Department> depList = departmentService.findAllOrderByDepId();
		for(Department dep : depList) {
			depMap.put(dep.getDepartmentId(), dep.getDepartmentName()); // D01=総務部  D02=営業部  と ループで putされてる
		}		
		return depMap;
	}

}
