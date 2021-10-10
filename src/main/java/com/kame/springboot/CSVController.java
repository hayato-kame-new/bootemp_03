package com.kame.springboot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kame.springboot.model.Employee;

@Controller
public class CSVController {
	@Autowired
	HttpSession session;
	
	@RequestMapping(value = "/csv", method = RequestMethod.GET )
	public String csv(RedirectAttributes redirectAttributes) {
		// 異なるコントローラ間なので、セッションスコープから取得する
		List employeeList = (List) session.getAttribute("employeeList");
		// list はArrayList<E> です。中には要素としてObject型の配列オブジェクトObject[11]が入ってます。
		
		// 新しいリストに詰め直す
		Employee emp = null;
		 List<Employee> employeeNewList = new ArrayList<Employee>();
		
		Iterator itr = employeeList.iterator();
		while (itr.hasNext()) {
			Object[] object = (Object[]) itr.next(); // next() 反復処理で次の要素を返します。
			// Object型から変換しながら、取得
			String employeeId = String.valueOf(object[0]);  //  左辺は   (String) object[0];  でもいい
			String name = (String) object[1]; // 左辺は   String.valueOf(obj[1]);   でもいい
			int age = (int) object[2]; // 左辺は   int age = Integer.parseInt(String.valueOf(obj[2]));   でもいい
			int gender = Integer.parseInt(String.valueOf(object[3])); // 左辺は (int) object[3];  でもいい
			int photoId = (int) object[4];   // 左辺は  int photoId = Integer.parseInt(String.valueOf(obj[4]));  でもいい
			String zipNumber = (String) object[5]; // 左辺は String.valueOf(obj[5]);  でもいい
			String pref = (String) object[6];  // 左辺は String.valueOf(obj[6]);
			String address = (String) object[7];  // 左辺は String.valueOf(obj[7]);
			String departmentId = (String) object[8];  // 左辺は String.valueOf(obj[8]);
			
			// 入社日 Object型から文字列にして
			String str = (new SimpleDateFormat("yyyy-MM-dd")).format(object[9]);
			// 文字列から java.util.Date型へ変換
			java.util.Date utilHireDate = null;
			try {
				utilHireDate = (new SimpleDateFormat("yyyy-MM-dd")).parse(str);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// 入社日は、下の２行でもOK
			// java.sql.Date sqlHireDate = (Date)object[9];
			// java.util.Date utilHireDate = new java.util.Date(sqlHireDate.getTime());
			
			// 退社日
			java.sql.Date sqlRetirementDate = null;
			java.util.Date utilRetirementDate = null;
			if (object[10] != null) {
				sqlRetirementDate = (java.sql.Date) object[10];
				utilRetirementDate = new java.util.Date(sqlRetirementDate.getTime());
			}
			
			// employeeインスタンスの各フィールドへセットする
//			employee.setEmployeeId(employeeId); 
//			employee.setName(name);
//			employee.setAge(age);
//			employee.setGender(gender);
//			employee.setPhotoId(photoId);
//			employee.setZipNumber(zipNumber);
//			employee.setPref(pref);
//			employee.setAddress(address);
//			employee.setDepartmentId(departmentId); 
//			 employee.setHireDate(utilHireDate);
//			employee.setRetirementDate(utilRetirementDate);

			// 引数ありコンストラクタを使うと
			 emp = new Employee(employeeId, name, age, gender, photoId,
			 zipNumber, pref, address, departmentId, utilHireDate,
			 utilRetirementDate);
			 employeeNewList.add(emp); 			
		}
				
		String file_name = "/csv_result.csv"; //  拡張子も書く
		// Fileクラスのオブジェクトを作成 ユーザのデスクトップにファイルを作ろうとしている
		File file = new File(System.getProperty("user.home") + "/Desktop" + file_name);
		
		try {
			file.createNewFile();  // その名前のファイルがまだ存在していない場合だけ、ファイルを作る。  戻り値は 指定されたファイルが存在せず、ファイルの生成に成功した場合はtrue、示されたファイルがすでに存在する場合はfalse
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(file.exists()) {
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			BufferedWriter bw = null;
			try {
				fos = new FileOutputStream(file);
				// 文字コードを指定して ファイルに書き込みます
				osw = new OutputStreamWriter(fos, "UTF-8");
				bw = new BufferedWriter(osw);
				// 見出し部分
				bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n", "社員ID", "名前", "年齢", "性別", "写真ID", "住所", "部署ID",
	                        "入社日", "退社日"));

				 for(Employee employee : employeeNewList) {  // ４番目のフォーマットは、文字列にして表示してます。 %s  にしてます
					 bw.write(String.format("%s,%s,%d,%s,%d,%s,%s,%tF,%tF\n", employee.getEmployeeId(), employee.getName(), employee.getAge(), employee.getStringGender(employee.getGender()), employee.getPhotoId(), employee.getFullAddress(), employee.getDepartmentId(), employee.getHireDate(), employee.getRetirementDate()));
				 }
				 bw.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally { // 最後にfinally句で  bw.close() osw.close() fos.close() の順番で クローズ処理する
				if(bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(osw != null) {
					try {
						osw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}		
		//  Flash Scop へ、インスタンスをセットできます。 Flash Scopは、１回のリダイレクトで有効なスコープです。 Request Scope より長く、Session Scope より短いイメージ
		// addFlashAttributeメソッドです  Flash Scope 使う RedirectAttributes redirectAttributes をリクエストハンドラの引数に書く
		redirectAttributes.addFlashAttribute("employeeList", employeeList); // セッションスコープから取得したものを、またセットする
		String flashMsg = "デスクトップに CSVファイルを出力しました。";
		redirectAttributes.addFlashAttribute("flashMsg", flashMsg);
		redirectAttributes.addFlashAttribute("action", "csv");
		// リダイレクトする
		return "redirect:/employee";
	}
	
}
