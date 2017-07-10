package com.maachang.httpPush;

import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.util.Config;
import com.maachang.httpPush.util.Utils;
import com.maachang.httpPush.util.shutdown.ShutdownClient;

/**
 * サーバシャットダウン処理.
 */
public final class HttpPushDown {
	private HttpPushDown() {
	}

	/** サーバー起動処理. **/
	public static final void main(String[] args) throws Exception {

		// パラメータからのポート指定がない場合は、サーバ定義を読み込み、
		// そこからポート番号で処理.
		if (args == null || args.length == 0) {

			// コンフィグファイルが存在するかチェック.
			Config conf = new Config();
			if (!Config.read(conf, HttpPush.CONF_NAME)) {
				System.out.println("file:" + Def.CONF_FILE + "の読み込みに失敗.");
				return;
			}
			int shutdownPort = conf.getInt("httpPush", "shutdownPort", 0);

			ShutdownClient.send(shutdownPort);
		}
		// パラメータ指定されている場合は、その内容を利用する.
		else {
			int port = Utils.convertInt(args[0]);
			ShutdownClient.send(port);
		}
	}
}
