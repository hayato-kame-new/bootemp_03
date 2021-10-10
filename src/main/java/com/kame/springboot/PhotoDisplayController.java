package com.kame.springboot;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kame.springboot.service.PhotoService;

@Controller
public class PhotoDisplayController {
	
	@Autowired
	PhotoService photoService;
	
	/**
	 * 画像を表示する. 戻り値voidです.直接レスポンスを書き込む
	 * 
	 * @param photoId
	 * @param response
	 */
	@RequestMapping(value = "/getImg", method = RequestMethod.GET)
	public void getImg(
			@RequestParam(name = "photoId")int photoId, // 必須パラメータ
			HttpServletResponse response) {
		// 新規作成の時には、 photoIdは、int型のデータ型の規定値で 0 が入ってきています.
		// HttpServletResponse response を使って、戻り値を voidにすれば、直接レスポンスを書き込むことができる.
		// photoid は、imgタグのリンクのURLの末尾にクエリー文字列として、送ってきてます. 
		// クエリー文字列で送られたものは リクエストハンドラで@RequestParamを使って取得することができます.
		// 社員新規作成の時、photoId が 0 なので javax.persistence.NoResultException 発生します.
		// photoId が 0 の時には、getPhotoDataメソッドを呼び出さないこと.
		if (photoId != 0) { // 編集の時だけ、表示する
			byte[] photoData = photoService.getPhotoData(photoId); 
			String mime = photoService.getMime(photoId); // コンテンツタイプの取得  "image/jpeg"  "image/png" など "タイプ/サブタイプ" という形
			// photoIdが存在するが、データベースでレコードを直接作ったものは、photoDataが nullで登録しているものもあるから
			if(photoData != null) { // 取得したものが null じゃなければ出力する
				try {
					ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
					byteOutStream.write(photoData);
					response.setContentType(mime);
					OutputStream out = response.getOutputStream();
					out.write(byteOutStream.toByteArray());  // byte[] に変換して書き込む
					out.flush();
					out.close();  
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}				
	}
}
