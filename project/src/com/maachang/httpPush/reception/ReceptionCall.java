package com.maachang.httpPush.reception;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.maachang.httpPush.data.PushData;
import com.maachang.httpPush.data.PushDataManager;
import com.maachang.httpPush.net.NioCall;
import com.maachang.httpPush.net.NioElement;
import com.maachang.httpPush.net.SendData;
import com.maachang.httpPush.net.http.HttpAnalysis;
import com.maachang.httpPush.net.http.HttpRequest;
import com.maachang.httpPush.net.http.HttpStatus;
import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.sse.SseElement;
import com.maachang.httpPush.util.ByteArrayIO;
import com.maachang.httpPush.util.ListMap;
import com.maachang.httpPush.util.sequence.Time16SequenceId;

/**
 * Reception(受付)コール処理.
 */
public final class ReceptionCall extends NioCall {
	private static final Log LOG = LogFactory.getLog(ReceptionCall.class);
	private PushDataManager manager;

	/**
	 * コンストラクタ.
	 */
	public ReceptionCall(PushDataManager m) {
		manager = m;
	}

	/**
	 * 新しい通信要素を生成.
	 * 
	 * @return BaseNioElement 新しい通信要素が返却されます.
	 */
	public NioElement createElement() {
		return new ReceptionElement();
	}

	/**
	 * 開始処理.
	 * 
	 * @return boolean [true]の場合、正常に処理されました.
	 */
	public boolean startNio() {
		LOG.info("##### start Reception nio");
		return true;
	}

	/**
	 * 終了処理.
	 */
	public void endNio() {
		LOG.info("##### stop Reception nio");
	}

	/**
	 * エラーハンドリング.
	 */
	public void error(Throwable e) {
		LOG.error("##### error Reception nio", e);
	}

	/**
	 * Accept処理.
	 * 
	 * @param em
	 *            対象のBaseNioElementオブジェクトが設定されます.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	public boolean accept(NioElement em) throws IOException {
		LOG.debug("##### accept Reception nio");

		return true;
	}

	/**
	 * Send処理.
	 * 
	 * @param em
	 *            対象のBaseNioElementオブジェクトが設定されます.
	 * @param buf
	 *            対象のByteBufferを設定します.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	public boolean send(NioElement em, ByteBuffer buf) throws IOException {
		LOG.debug("##### send Reception nio");

		ReceptionElement rem = (ReceptionElement) em;

		// 送信データを取得.
		SendData sendData = rem.getSendData();
		byte[] b = sendData.get();

		// 送信対象のデータが存在しない場合.
		if (b == null) {

			// データが存在しない場合.
			if (buf.position() == 0) {

				// 通信切断処理.
				return false;
			}
			return true;
		}

		// 送信処理.
		int p = sendData.getPosition();
		int n = b.length - p;
		int len = buf.limit() - buf.position();
		if (len > n) {
			buf.put(b, p, n);
			sendData.clear();
		} else {
			buf.put(b, p, len);
			sendData.setPosition(p + len);
		}
		return true;
	}

	/**
	 * Receive処理.
	 * 
	 * @param em
	 *            対象のBaseNioElementオブジェクトが設定されます.
	 * @param buf
	 *            対象のByteBufferを設定します.
	 * @return boolean [true]の場合、正常に処理されました.
	 * @exception IOException
	 *                IO例外.
	 */
	public boolean receive(NioElement em, ByteBuffer buf) throws IOException {
		LOG.debug("##### recv Reception nio:" + buf);

		ReceptionElement rem = (ReceptionElement) em;

		// 受信バッファに今回分の情報をセット.
		ByteArrayIO buffer = rem.getBuffer();
		buffer.write(buf);

		// Httpリクエストを取得.
		HttpRequest request = rem.getRequest();
		if (request == null) {

			// HTTPリクエストが存在しない場合は、新規作成.
			int endPoint = HttpAnalysis.endPoint(buffer);
			if (endPoint == -1) {

				// 受信途中の場合.
				return true;
			}
			request = HttpAnalysis.getRequest(buffer, endPoint);
			rem.setRequest(request);
		}
		// OPTIONの場合は、Optionヘッダを返却.
		if ("OPTIONS".equals(request.getMethod())) {

			rem.setRequest(null);
			rem.getSendData().set(OPSIONS_RESPONSE);
			return true;
		}
		// POSTの場合は、ContentLength分の情報を取得.
		else if ("POST".equals(request.getMethod())) {

			// ContentLengthを取得.
			int contentLength = request.getContentLength();
			if (contentLength == -1) {

				// 存在しない場合はコネクション強制クローズ.
				// chunkedの受信は対応しない.
				return false;
			}

			// Body情報が受信完了かチェック.
			if (buffer.size() >= contentLength) {
				byte[] body = new byte[contentLength];
				buffer.read(body);
				request.setBody(body);
			} else {

				// PostのBody受信中.
				return true;
			}
		}

		// 処理実行.
		if (!executeRequest(rem, request)) {
			return false;
		}
		return true;
	}

	/** URLを分解. **/
	private static final List<String> parseURL(String url) {
		List<String> ret = new ArrayList<String>();
		int p, b, len;
		len = url.indexOf("?");
		if (len == -1) {
			len = url.length();
		}
		b = 1;
		while ((p = url.indexOf("/", b)) != -1) {
			if (p > len) {
				break;
			}
			ret.add(url.substring(b, p));
			b = p + 1;
		}
		if (b < len) {
			ret.add(url.substring(b, len));
		}
		return ret;
	}

	/** 受信結果に対して、URLの条件を実行. **/
	private final boolean executeRequest(ReceptionElement rem,
			HttpRequest request) throws IOException {

		// URLを解析して、処理条件を取得.
		List<String> params = parseURL(request.getUrl());

		// 第一引数には、処理命令.
		if (params.size() == 0) {
			errorResponse(rem, 403);
			return true;
		}
		String method = params.get(0);
		method = method.toLowerCase();

		// 新規接続.
		if ("open".equals(method) || "create".equals(method)) {
			return executeOpen(params, rem, request);
		}
		// 切断処理.
		else if ("close".equals(method) || "clear".equals(method)) {
			return executeClose(params, rem, request);
		}
		// データセット.
		else if ("push".equals(method) || "put".equals(method)
				|| "send".equals(method)) {
			return executePush(params, rem, request);
		}
		// データ数.
		else if ("size".equals(method) || "length".equals(method)) {
			return executeSize(params, rem, request);
		}

		// 不明な処理の場合 404例外.
		errorResponse(rem, 404);
		return true;
	}

	/** 処理オープン. **/
	private final boolean executeOpen(List<String> params,
			ReceptionElement rem, HttpRequest request) throws IOException {

		// 新規のUUIDを生成し、JSON返却.
		String uuid = manager.createId();

		// 情報が存在しない場合は生成.
		// ここでは作成しない.
		// if(!manager.isUUID(uuid)) {
		// manager.create(uuid);
		// }

		// レスポンスデータ.
		String res = new StringBuilder("{\"uuid\": \"").append(uuid).append(
				"\"}").toString();

		// 処理結果を返却.
		rem.setRequest(null);
		rem.getSendData().set(stateResponse(200, res));
		return true;
	}

	/** 処理クローズ. **/
	private final boolean executeClose(List<String> params,
			ReceptionElement rem, HttpRequest request) throws IOException {

		// パラメータが足りない場合.
		if (params.size() <= 1) {
			errorResponse(rem, 403);
			return true;
		}

		boolean result = false;

		// UUIDのパラメータチェック.
		String uuid = params.get(1);
		if (Time16SequenceId.getBytes(uuid) == null) {

			// パラメータがおかしい場合.
			result = false;
		} else {

			// 削除処理.
			result = manager.clear(uuid);
		}
		String res = new StringBuilder("{\"result\": ").append(result).append(
				"}").toString();

		// 処理結果を返却.
		rem.setRequest(null);
		rem.getSendData().set(stateResponse(200, res));
		return true;
	}

	/** 処理データセット. **/
	private final boolean executePush(List<String> params,
			ReceptionElement rem, HttpRequest request) throws IOException {

		// パラメータが足りない場合.
		// POST通信でない場合.
		int paramsSize = params.size();
		if (paramsSize <= 1) {
			errorResponse(rem, 403);
			return true;
		}

		boolean result = true;

		// UUIDのパラメータチェック.
		String uuid = params.get(1);
		if (Time16SequenceId.getBytes(uuid) == null) {

			// パラメータがおかしい場合.
			result = false;
		} else {

			// 情報が存在しない場合は生成.
			if (!manager.isUUID(uuid)) {
				manager.create(uuid);
			}

			// Pushデータ生成.
			PushData data = null;
			if ("POST".equals(request.getMethod())) {

				// POST全体のデータをセット.
				data = new PushData(new String(request.getBody(), "UTF8"));
			} else {

				// GETで設定されているデータを取得.
				// その中で、キーは[data]を送信対象とする.
				String url = request.getUrl();
				int p = url.indexOf("?");
				if (p == -1) {
					errorResponse(rem, 403);
					return true;
				}
				ListMap map = HttpAnalysis.paramsAnalysis(url, p + 1);
				String d = map.get("data");
				if (d == null) {
					errorResponse(rem, 403);
					return true;
				}
				data = new PushData(d);
			}

			// event条件をセット.
			if (paramsSize > 2) {
				data.setEvent(params.get(2));
			}
			// retry条件をセット.
			if (paramsSize > 3) {
				data.setRetry(params.get(3));
			}
			// id条件をセット.
			if (paramsSize > 4) {
				data.setId(params.get(4));
			}
			// データセット.
			manager.offer(uuid, data);

			// SSE側の要素が存在し、接続中の場合、
			// 今回セットした内容をデータセットする.
			SseElement sem = manager.getElement(uuid);
			if (sem != null && sem.isConnection()) {

				// 送信モードセット.
				sem.sendMode();
			}
		}
		String res = new StringBuilder("{\"result\": ").append(result).append(
				"}").toString();

		// 処理結果を返却.
		rem.setRequest(null);
		rem.getSendData().set(stateResponse(200, res));

		return true;
	}

	/** 現在のデータ数を取得. **/
	private final boolean executeSize(List<String> params,
			ReceptionElement rem, HttpRequest request) throws IOException {

		// パラメータが足りない場合.
		if (params.size() <= 1) {
			errorResponse(rem, 403);
			return true;
		}

		boolean result = false;
		int size = 0;

		// UUIDのパラメータチェック.
		String uuid = params.get(1);
		if (Time16SequenceId.getBytes(uuid) == null) {

			// パラメータがおかしい場合.
			result = false;
		} else {

			// 削除処理.
			result = true;
			try {
				size = manager.length(uuid);
			} catch (Exception e) {
				size = 0;
			}
		}
		String res = new StringBuilder("{\"result\": ").append(result).append(
				",\"size\": ").append(size).append("}").toString();

		// 処理結果を返却.
		rem.setRequest(null);
		rem.getSendData().set(stateResponse(200, res));
		return true;
	}

	/** ステータス指定Response返却用バイナリの生成. **/
	private static final byte[] stateResponse(int state, String body)
			throws IOException {
		byte[] stateBinary = new StringBuilder(String.valueOf(state)).append(
				" ").append(HttpStatus.getMessage(state)).toString().getBytes(
				"UTF8");

		byte[] b = body.getBytes("UTF8");
		byte[] foot = (String.valueOf(b.length) + "\r\n\r\n").getBytes("UTF8");
		int all = STATE_RESPONSE_1.length + stateBinary.length
				+ STATE_RESPONSE_2.length + foot.length + b.length;
		byte[] ret = new byte[all];

		int pos = 0;
		System
				.arraycopy(STATE_RESPONSE_1, 0, ret, pos,
						STATE_RESPONSE_1.length);
		pos += STATE_RESPONSE_1.length;
		System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
		pos += stateBinary.length;
		System
				.arraycopy(STATE_RESPONSE_2, 0, ret, pos,
						STATE_RESPONSE_2.length);
		pos += STATE_RESPONSE_2.length;
		System.arraycopy(foot, 0, ret, pos, foot.length);
		pos += foot.length;
		System.arraycopy(b, 0, ret, pos, b.length);
		return ret;
	}

	/** エラーレスポンスを送信. **/
	private final void errorResponse(ReceptionElement rem, int status)
			throws IOException {
		String res = new StringBuilder("{\"result\": false, \"status\": ")
				.append(status).append("}").toString();

		// 処理結果を返却.
		rem.setRequest(null);
		rem.getSendData().set(stateResponse(status, res));
	}

	/** Optionsレスポンス. **/
	private static final byte[] OPSIONS_RESPONSE;

	/** ステータス指定レスポンス. **/
	private static final byte[] STATE_RESPONSE_1;
	private static final byte[] STATE_RESPONSE_2;

	static {
		byte[] b;
		byte[] s1;
		byte[] s2;
		try {
			b = ("HTTP/1.1 200 OK\r\n" + "Allow: OPTIONS, GET, POST\r\n"
					+ "Cache-Control: no-cache\r\n"
					+ "Access-Control-Allow-Origin: *\r\n" + "Server: "
					+ Def.RECEPTION_SERVER_NAME + "\r\n"
					+ "Connection: close\r\n" + "Content-Length: 0\r\n\r\n")
					.getBytes("UTF8");

			s1 = ("HTTP/1.1 ").getBytes("UTF8");
			s2 = ("\r\n" + "Cache-Control: no-cache\r\n"
					+ "Access-Control-Allow-Origin: *\r\n" + "Server: "
					+ Def.RECEPTION_SERVER_NAME + "\r\n"
					+ "Connection: close\r\n"
					+ "Content-Type: application/json\r\n" + "Content-Length: ")
					.getBytes("UTF8");

		} catch (Exception e) {
			b = null;
			s1 = null;
			s2 = null;
		}
		OPSIONS_RESPONSE = b;
		STATE_RESPONSE_1 = s1;
		STATE_RESPONSE_2 = s2;
	}
}
