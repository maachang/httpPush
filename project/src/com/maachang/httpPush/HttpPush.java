package com.maachang.httpPush;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.maachang.httpPush.data.PushDataManager;
import com.maachang.httpPush.data.ShutdownHttpPush;
import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.reception.Reception;
import com.maachang.httpPush.reception.ReceptionInfo;
import com.maachang.httpPush.sse.Sse;
import com.maachang.httpPush.sse.SseInfo;
import com.maachang.httpPush.util.Config;
import com.maachang.httpPush.util.Utils;
import com.maachang.httpPush.util.sequence.Time16SequenceId;
import com.maachang.httpPush.util.shutdown.ShutdownHook;
import com.maachang.httpPush.util.shutdown.WaitShutdown;

/**
 * HttpPushサービス.
 */
public final class HttpPush {

	/** ログ. **/
	private static final Log LOG = LogFactory.getLog(HttpPush.class);

	/** 読み込みファイル名. **/
	protected static final String CONF_NAME = Def.CONF_PATH + Def.CONF_FILE;

	/** Main. **/
	public static final void main(String[] args) {
		LOG.info("########## startup HttpPush");
		try {
			HttpPush httpPush = new HttpPush();
			httpPush.execute();
		} catch (Throwable e) {
			LOG.error("error", e);
		} finally {
			LOG.info("$$$$$$$$$$ exit HttpPush");
		}
	}

	/** 定義読み取り. **/
	protected final Config conf = new Config();

	/** manager. **/
	protected PushDataManager manager;

	/** Reception. **/
	protected Reception reception;

	/** Sse. **/
	protected Sse sse;

	/** 起動処理. **/
	protected final void execute() throws Exception {
		Time16SequenceId seq = null;

		LOG.info("# readConfig.");

		// コンフィグファイルが存在するかチェック.
		if (!Config.read(conf, CONF_NAME)) {
			error("file:" + Def.CONF_FILE + "の読み込みに失敗.");
			return;
		}

		LOG.info("# load PushData Manager.");

		// 以前の保存データが存在しない場合は、シーケンス管理オブジェクトを生成.
		if (!Utils.isFile(Def.HTTP_PUSH_DATA)) {
			seq = new Time16SequenceId(conf.getInt("httpPush", "machineId", 0));
		}

		// Dataマネージャを生成.
		manager = new PushDataManager(seq, conf.getInt("data", "minLength", 0),
				conf.getLong("data", "minTimeout", 0), conf.getLong("data",
						"sessionTimeout", 0));

		// 以前の保存データが存在する場合.
		if (Utils.isFile(Def.HTTP_PUSH_DATA)) {
			LOG.info("** read " + Def.HTTP_PUSH_DATA);
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(
						Def.HTTP_PUSH_DATA));
				manager.load(in);
				in.close();
				in = null;
			} catch (Exception e) {
				LOG.error("file:" + Def.HTTP_PUSH_DATA + "の読み込みに失敗しました", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ee) {
					}
				}
			}
		}

		LOG.info("# load PushData Reception.");

		// Receptionの生成.
		ReceptionInfo receptionInfo = new ReceptionInfo();
		loadReceptionInfo(receptionInfo, conf);
		reception = new Reception(receptionInfo, manager);

		LOG.info("# load PushData Sse.");

		// Sseの生成.
		SseInfo sseInfo = new SseInfo();
		loadSseInfo(sseInfo, conf);
		sse = new Sse(sseInfo, manager);

		reception.start();
		sse.start();

		// シャットダウンまで待つ処理を生成.
		int shutdownPort = conf.getInt("httpPush", "shutdownPort", 0);

		// シャットダウンフックセット.
		ShutdownHttpPush sd = new ShutdownHttpPush(reception, sse, manager);
		ShutdownHook.registHook(sd);

		// サーバーシャットダウン待ち.
		WaitShutdown.waitSignal(shutdownPort, 0);
	}

	/** エラー出力. **/
	protected final void error(String errMessage) {
		LOG.error(errMessage);
		System.exit(-1);
	}

	/** Reception設定データを取得. **/
	protected static final void loadReceptionInfo(ReceptionInfo info,
			Config conf) throws Exception {

		String section = "reception";
		Object o = null;

		o = conf.getInt(section, "backlog", 0);
		if (o != null) {
			info.setBacklog((Integer) o);
		}

		o = conf.getInt(section, "byteBufferLength", 0);
		if (o != null) {
			info.setByteBufferLength((Integer) o);
		}

		o = conf.getInt(section, "socketSendBuffer", 0);
		if (o != null) {
			info.setSocketSendBuffer((Integer) o);
		}

		o = conf.getInt(section, "socketReceiveBuffer", 0);
		if (o != null) {
			info.setSocketReceiveBuffer((Integer) o);
		}

		o = conf.get(section, "localAddress", 0);
		if (o != null) {
			info.setLocalAddress((String) o);
		}

		o = conf.getInt(section, "localPort", 0);
		if (o != null) {
			info.setLocalPort((Integer) o);
		}
	}

	/** SSE設定データを取得. **/
	protected static final void loadSseInfo(SseInfo info, Config conf)
			throws Exception {

		String section = "sse";
		Object o = null;

		o = conf.getInt(section, "backlog", 0);
		if (o != null) {
			info.setBacklog((Integer) o);
		}

		o = conf.getInt(section, "byteBufferLength", 0);
		if (o != null) {
			info.setByteBufferLength((Integer) o);
		}

		o = conf.getInt(section, "socketSendBuffer", 0);
		if (o != null) {
			info.setSocketSendBuffer((Integer) o);
		}

		o = conf.getInt(section, "socketReceiveBuffer", 0);
		if (o != null) {
			info.setSocketReceiveBuffer((Integer) o);
		}

		o = conf.get(section, "localAddress", 0);
		if (o != null) {
			info.setLocalAddress((String) o);
		}

		o = conf.getInt(section, "localPort", 0);
		if (o != null) {
			info.setLocalPort((Integer) o);
		}
	}
}
