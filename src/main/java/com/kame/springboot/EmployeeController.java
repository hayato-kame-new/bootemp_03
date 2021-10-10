package com.kame.springboot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kame.springboot.component.ViewBean;
import com.kame.springboot.model.Employee;
import com.kame.springboot.service.DepartmentService;
import com.kame.springboot.service.EmployeeService;
import com.kame.springboot.service.PhotoService;
// コントローラでは、@Transactionalつけないこと. サービスクラスを利用するので、 サービスのクラスの方に@Transactionalアノテーションをつけて、コントローラのリクエストハンドラでtry-catchするため.
@Controller // コンポーネントです  
public class EmployeeController { 

	// @Autowiredによって自動で、内部クラスのインスタンスが生成されて利用できる
	@Autowired
	EmployeeService employeeService;

	@Autowired
	PhotoService photoService;

	@Autowired
	DepartmentService departmentService;

	// コントローラでは、 ビューのコンポーネントをフィールドとしてもつ @Autowiredによって自動で、内部クラスのインスタンスが生成されてる
	@Autowired
	ViewBean viewBean;
	
	// indexリクエストハンドラでセッションスコープ使う、異なるコントローラCSVControllerと共有する セッションスコープBeanをつかうため
	@Autowired
	HttpSession session;
	
	  // ファイルのアップロード時のパターンチェック
    private final java.util.regex.Pattern PATTERN_IMAGE = java.util.regex.Pattern.compile("^image\\/(jpeg|jpg|png)$");


    /**
	 * 社員一覧表示.
	 * 
	 * @param mav
	 * @return mav
	 */
	@SuppressWarnings({  "unchecked" })
	@RequestMapping(value = "/employee", method = RequestMethod.GET)
	public ModelAndView index(
			Model model, // Flash Scopeから値の取り出しに必要
			ModelAndView mav) {
		String title = "index";
		// 削除後 検索後 CSV出力後 など、リダイレクトしてくる  フラッシュメッセージ Flash Scopeから値の取り出し Model model を引数に書いて、 modelインスタンスのgetAttribute(キー）で値を取得
		String flashMsg = "";
		if (model.getAttribute("flashMsg") != null){  // 最初の画面一覧を表示するとき、model.getAttribute("flashMsg") は null なので			
			flashMsg = (String) model.getAttribute("flashMsg");// 返り値がObject型なので、キャストすること
		}
		// Flash Scopeから取り出すには、Modelインスタンスの getAttributeメソッドを使う

		// 一番最初に 一覧表示する時に null が入ってる
		String action = (String) model.getAttribute("action"); // Flash Scopeから取り出す			
		
		List<Employee> employeeList = new ArrayList<Employee>();
		// 社員一覧を表示する時
		if (action == null) {
			employeeList = employeeService.getEmpListOrderByAsc(); // 一覧を辞書順で、昇順で取得する
		}
		// 検索結果を出した後 リダイレクトしてきた時
		if(action != null && action.equals("find")) {  // 先に action != null を書いてnullチェックすること
			employeeList = (List<Employee>) model.getAttribute("employeeList"); // 検索結果をFlash Scopeから取り出す
			title = "find result";
			mav.addObject("action", action);
		}
		// CSVファイル出力した後に、リダイレクトしてきた時
		if(action != null && action.equals("csv")) {  // 先に action != null を書いてnullチェックすること
			employeeList = (List<Employee>) model.getAttribute("employeeList"); // 検索結果をFlash Scopeから取り出す
			title = "csv";
		}
		// CSVControllerで使いたいので、セッションスコープへ保存 異なるコントローラCSVControllerで使いたいのでセッションスコープへ 保存する
		session.setAttribute("employeeList", employeeList); 
		mav.setViewName("employee");
		mav.addObject("title", title);
		mav.addObject("flashMsg", flashMsg); // 検索結果が0の時には、検索結果が見つからないメッセージ
		mav.addObject("employeeList", employeeList);
		return mav;
	}

	/**
	 * 新規登録・編集 画面表示.
	 * 
	 * @param action
	 * @param employeeId
	 * @param employee
	 * @param mav
	 * @return mav
	 */
	@RequestMapping(value = "emp_add_edit", method = RequestMethod.GET)
	public ModelAndView empDisplay(@RequestParam(name = "action") String action,
			@RequestParam(name = "employeeId", required = false) String employeeId,
			@ModelAttribute("formModel") Employee employee, ModelAndView mav) {

		mav.setViewName("employeeAddEdit");
		mav.addObject("action", action);
		mav.addObject("title", action);
		// 性別ラジオボタン表示用
		Map<Integer, String> genderMap = viewBean.getGenderMap();
		mav.addObject("genderMap", genderMap);	
		// 都道府県セレクトタグのドロップボタン表示用
		Map<Integer, String> prefMap = viewBean.getPrefMap();
		mav.addObject("prefMap", prefMap);		
		// 部署セレクトタグのドロップボタン表示用
		Map<String, String> depMap = viewBean.getDepartmentMap(); // 取れてる {D01=総務部, D02=営業部, D03=開発部, D06=営業部９９９, D07=A部, D08=あいう, D09=新しい部署}
		mav.addObject("depMap", depMap);
				
		switch (action) {
		case "add":
			// 新規だと、@ModelAttributeによって 用意されたemployee変数には すでに空のEmployeeインスタンスが用意されている(各フィールドには、各データ型の規定値が入ってる)ので このままbreak; で
			break; // switch文を抜ける
		case "edit":
			// 編集のときは、@ModelAttributeによって 用意されたemployee変数(各フィールドが規定値)を上書きしてから、addObjectでセットする
			 employee = employeeService.getEmp(employeeId);  // 編集だと、employeeIdの値が hiddenで送られてくる
			break; // switch文を抜ける
		}
		mav.addObject("formModel", employee);
		return mav;
	}

	
	// SpringBootでは、デフォルトでファイルサイズの上限が1MB(1024*1024=1048576bytes)となっています。
	// アップロードしたファイルのサイズがこれより大きい場合、MaxUploadSizeExceededExceptionがスローされ、リクエストは処理されません。
	// propertiesファイルの場合 以下の２行を加えてください。
	// spring.servlet.multipart.max-file-size=30MB
	// spring.servlet.multipart.max-request-size=30MB
	/**
	 * 新規登録 編集する.
	 * 
	 * @param action
	 * @param employeeId
	 * @param multipartFile
	 * @param employee
	 * @param result
	 * @param mav
	 * @return mav
	 */
	@RequestMapping(value = "emp_add_edit", method = RequestMethod.POST)
	@Transactional(readOnly=false)
	public ModelAndView empAddUpdate(@RequestParam(name = "action") String action,
			@RequestParam(name = "employeeId", required = false) String employeeId,
		    @RequestParam(name = "upload_file", required = false) MultipartFile multipartFile,
			@ModelAttribute("formModel") @Validated Employee employee, BindingResult result, ModelAndView mav) {

		ModelAndView resMav = null;
		// バリデーションOKなら結果ページへ送る バリデーションエラーの時は、入力画面へ送る
		String title = "";
		String msg = "";
		
		// アップロードされたファイルは「org.springframework.web.multipart.MultipartFile」で受け取ります。
		// file.isEmpty()メソッド。値がnullの場合trueとなるが、ファイルサイズが0の場合もtrueとなるため、空ファイルの判定もできる。
		boolean part = multipartFile.isEmpty(); // アップロードしてこないと true 空のファイルでも trueとなり、空ファイルの判定もできる
		long size = multipartFile.getSize(); // 3633674 ファイルアップロードしない時には、 0 と入ってきてる
		String mime = multipartFile.getContentType(); // contentTypeを取得します。 "image/jpeg" など入ってる アップロードをしてこない時は
														// application/octet-stream となる
		// 新規の時に、ファイルをアップロード必須にする、アノテーションをつけずに(つけられないから)、エラーメッセージに追加し、エラーメッセージをフォームのところで表示。
		// FieldErrorオブジェクトを生成して、ResultインスタンスのインスタンスメソッドaddErrorで エラーメッセージに追加
		if (action.equals("add") && multipartFile.isEmpty()) {  // アノテーションをつけずに、エラーメッセージを追加して表示
			FieldError fieldError = new FieldError(result.getObjectName(), "photoId", "画像ファイルを選択してください"); //result.getObjectName()  で "formModel" が取れる
			result.addError(fieldError);
		}
		// ファイルのアップロードがあり、かつ、パターンチェックに合ってない時は、エラーメッセージに追加する
		if(!multipartFile.isEmpty() &&  !PATTERN_IMAGE.matcher(mime).matches()) {
			FieldError fieldError = new FieldError(result.getObjectName(), "photoId", "画像の形式はJPEGまたはJPGおよびPNGにしてください");
			result.addError(fieldError);
		}

		// バリデーションエラーが発生していないのなら、処理へ進み、    バリデーションエラー発生したら、再入力してもらう入力画面へ送る
		if (!result.hasErrors()) { // バリデーションエラーが発生しないので、処理できる
			// 入力用のストリームを確保 新規の時に、ファイルをアップロード必須
			InputStream is = null;
			byte[] photoData = null;
			try {
				is = multipartFile.getInputStream();
				photoData = photoService.readAll_fromService(is); // 取得した画像ストリームを、byte配列に格納して返すメソッド
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// データベースの成功したかどうか
			boolean success = true;

			switch (action) {
			case "add":
				// 新規では、ファイルのアップロードを必須にしてる
				// photoテーブルを新規に登録する主キーのカラムのphotoid は自動採番する  成功すればtrue 失敗するとfalse が返る
				success = photoService.photoDataAdd(photoData, mime);
				if (!success) { // falseが返ったら、失敗
					msg = "写真データの新規登録に失敗しました。"; // 結果ページへの出力のため
					title = "失敗"; // 結果ページへの出力のため
					break; // switch文を抜ける
				} else { // tureが返ったら、成功   次はemployeeテーブルに新規作成をする
					// さっきphotoテーブルに登録した一番最後のphotoIdを取得して、 それをemployeeインスタンスのphotoIdの値に更新する
					int lastPhotoId = photoService.getLastPhotoId(); // 戻り値  データベースに登録されてる一番最後のphotoId(int型)が返る
					// まず、新規登録用に、社員IDを生成します。
					String generatedEmpId = employeeService.generateEmpId(); // 社員IDを生成
					// employee は、フォームからの値がセットされてるので、そのemployeeを更新する.その前に employeeのフィールドを上書きして更新する
					// セッターを使い、employeeIdフィールドに代入する(規定値null から上書きする)
					employee.setEmployeeId(generatedEmpId); // フォームから送られてきた時点ではemployeeIdの値は 規定値(String型の初期値)の null
															// になってるので、生成したIDで上書きする
					// セッターを使い、photoIdフィールドに代入する(規定値 0 から上書きする)
					employee.setPhotoId(lastPhotoId); // フォームから送られてきた時点ではphotoIdの値は 規定値(int型の初期値)の 0
														// になってるので、さっきphotoテーブルに新規登録した際に、自動生成されたphotoIdを  photoService.getLastPhotoId() によって取得してきたので、それで上書きする
					// 更新したemployeeを employeeテーブルに新規登録する
					success = employeeService.empAdd(employee);
					if (!success) { // 失敗
						msg = "社員データの新規登録に失敗しました。"; // 結果ページへの出力のため
						title = "失敗"; // 結果ページへの出力のため
						break; // switch文を抜ける
					}
					msg = "社員データを新規登録しました。";
					title = "成功";
				}
				break; // switch文を抜ける
			case "edit":
				// 編集では、ファイルのアップロードは無いかもしれない。なくてもOKにしてる
				// もし、ファイルアップロードあれば、編集では、employee.getPhotoId() で、photoId を取得できるので、上書きする photoテーブルの更新
				if(!multipartFile.isEmpty()) {
					success = photoService.photoDataUpdate(employee.getPhotoId(), photoData, mime); // photoテーブルの更新
					if(!success) {
						msg = "写真データの更新に失敗しました。";
						title = "失敗";
						break; // switch文を抜ける
					}
					// ここにきたらアップロードがあり、photoテーブルの更新が成功してる  
				}
				//@ModelAttributeによって  employee変数は すでにフォームから送られてきたデータがセットされてる   employeeテーブルの更新 
				success = employeeService.empUpdate(employee); 
				if(!success) {
					msg = "社員データの更新に失敗しました。";
					title = "失敗";
					break;// switch文を抜ける
				}
				msg = "社員データを更新しました。";
				title = "成功";
				break; // switch文を抜ける
			}

			mav.setViewName("result");
			mav.addObject("msg", msg);
			mav.addObject("title", title);
			mav.addObject("action", action);
			resMav = mav;

		} else { // バリデーションエラー発生したので、
			msg = "入力エラーが発生しました。";
			title = "入力エラー";

			mav.setViewName("employeeAddEdit");
			// 表示用 Resultオブジェクトから、前に入力してある値を取得します！！！
			Employee target = (Employee) result.getTarget();  // resultから
			// 性別ラジオボタン表示用
			Map<Integer, String> genderMap = viewBean.getGenderMap();
			mav.addObject("genderMap", genderMap);	
			mav.addObject("selectedGender" , target.getGender());  // 前のフォームで選択していたものを 選択済みのデータとして送る
			// 表示用 都道府県セレクトタグのドロップボタン表示用
			Map<Integer, String> prefMap = viewBean.getPrefMap();
			mav.addObject("prefMap", prefMap);
			mav.addObject("selectedPref" , target.getPref());  // 前のフォームで選択していたものを 選択済みのデータとして送る
			// 表示用 部署セレクトタグのドロップボタン表示用
			Map<String, String> depMap = viewBean.getDepartmentMap(); // 取れてる {D01=総務部, D02=営業部, D03=開発部, D06=営業部９９９, D07=A部, D08=あいう, D09=新しい部署}
			mav.addObject("depMap", depMap);
			mav.addObject("selectedDepartmentId", target.getDepartmentId()); // 前のフォームで選択をしたもの！！ 選択したままにする
						
			mav.addObject("msg", msg);
			mav.addObject("title", title);
			mav.addObject("action", action);
			resMav = mav;
		}

		return resMav;
	}
	
	/**
	 * 社員エンティティ削除.
	 * @param employeeId
	 * @param redirectAttributes
	 * @param mav
	 * @return
	 */
	@RequestMapping(value = "/emp_delete", method = RequestMethod.POST)
	@Transactional(readOnly=false)
	public String delete(
			@RequestParam(name = "employeeId") String employeeId,
			RedirectAttributes redirectAttributes,
			ModelAndView mav) {
		// リダイレクト先へフラッシュメッセージ
		String flashMsg = "社員データを削除しました。";
		boolean result = employeeService.deleteEmployee(employeeId);
		if(!result) {
			flashMsg = "社員データを削除できませんでした。";
		}
		//  Flash Scop へ、インスタンスをセットできます。 Flash Scopは、１回のリダイレクトで有効なスコープです。 Request Scope より長く、Session Scope より短いイメージ
		redirectAttributes.addFlashAttribute("flashMsg", flashMsg);
		return "redirect:/employee";	
	}

}
