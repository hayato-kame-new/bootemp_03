package com.kame.springboot.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component  // コンポーネントにする。Bean化して使うクラスになる
public class LogicBean {
	// このロジックのクラスは、サービスから呼び出して使う。ロジックは、サービス同士で共通の処理をまとめるための場所
	// @Autowiredを使って、サービスクラスのフィールドメンバにすると、自動でインスタンスを自動生成してくれる Beanインスタンスをメンバにする

	@Autowired  // このクラスを コンポーネントにするには、 コンストラクタに、@Autowired　を付けます
	public LogicBean() {
		super();
	}
	
	 /**
     * データを全て読み込んでbyte配列に格納して返すインスタンスメソッド.
     * このクラスがBeanとして登録されると、@Autowiredをつけてフィールドのメンバとして宣言するだけで、自動でBeanインスタンスが生成され利用できるようになる.
     * @param is
     * @return byte[]
     * @throws IOException
     */
    public byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(); // newで確保
        byte[] buffer = new byte[1024];  // 1024バイト  1キロバイト

        while (true) { // 無限ループ注意
            int len = is.read(buffer); // 1024バイトだけ取り込む
            if (len < 0) { // ループの終わりの条件
                break; // ループ抜ける break  必須  無限ループに注意 
            }
            bout.write(buffer, 0, len);  // 指定しただけoutputstreamにバッファに書き込みする　最後の端数も取り込めるようにする
        }
        return bout.toByteArray(); // 戻り値 byte[] です   toByteArray()で、byte[]に変換してる
        // 取り込んだものをバイト配列にして戻している
    }
}