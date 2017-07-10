package com.maachang.httpPush.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import com.maachang.httpPush.net.http.Json;
import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.util.ByteArrayIO;
import com.maachang.httpPush.util.sequence.Time16SequenceId;

/**
 * HttpPush Reception(受付処理)Client.
 */
@SuppressWarnings("unchecked")
public class ReceptionClient {
	protected static final int DEFAULT_TIMEOUT = 5000;
	protected String uuid = "";
	protected boolean ssl;
	protected String address;
	protected int port;
	protected int timeout;

	/**
	 * コンストラクタ.
	 * 
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public ReceptionClient(String address) throws IOException {
		this(false, address, defaultPort(false), DEFAULT_TIMEOUT);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param ssl
	 *            httpsで接続する場合は[true]を設定します.
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public ReceptionClient(boolean ssl, String address) throws IOException {
		this(ssl, address, defaultPort(ssl), DEFAULT_TIMEOUT);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            ポート番号を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public ReceptionClient(String address, int port) throws IOException {
		this(false, address, port, DEFAULT_TIMEOUT);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param ssl
	 *            httpsで接続する場合は[true]を設定します.
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            ポート番号を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public ReceptionClient(boolean ssl, String address, int port)
			throws IOException {
		this(ssl, address, port, DEFAULT_TIMEOUT);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            ポート番号を設定します.
	 * @param timeout
	 *            受信タイムアウト値を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public ReceptionClient(String address, int port, int timeout)
			throws IOException {
		this(false, address, port, timeout);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param ssl
	 *            httpsで接続する場合は[true]を設定します.
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            ポート番号を設定します.
	 * @param timeout
	 *            受信タイムアウト値を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public ReceptionClient(boolean ssl, String address, int port, int timeout)
			throws IOException {
		this.ssl = ssl;
		this.address = address;
		this.port = port;
		this.timeout = timeout;
	}

	/** デフォルトポート. **/
	private static final int defaultPort(boolean ssl) {
		return ssl ? 3444 : 3334;
	}

	/**
	 * 接続処理.
	 * 
	 * @param call
	 *            コールバック処理. 接続が成功した場合、UUIDが返却されます.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void connect(ReceptionCallback call) throws IOException {
		String method = "connect";
		if (uuid.length() != 0) {
			errorResult(method, call);
			return;
		}

		byte[] req = createHttpRequest("/create", null);
		new ReceiveReception(method, this, call, req);
	}

	/**
	 * 再接続.
	 * 
	 * @param uuid
	 *            再接続先のUUIDを設定します.
	 * @return boolean [true]の場合、正しく設定されました.
	 * @exception IOException
	 *                I/O例外.
	 */
	public boolean reconnect(String uuid) throws IOException {
		if (Time16SequenceId.getBytes(uuid) == null) {
			return false;
		}
		this.uuid = uuid;
		return true;
	}

	/**
	 * 切断処理.
	 * 
	 * @param call
	 *            コールバック処理. [true]が返却された場合、切断されました.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void disconnect(ReceptionCallback call) throws IOException {
		String method = "disconnect";
		if (uuid.length() == 0) {
			errorResult(method, call);
			return;
		}

		byte[] req = createHttpRequest("/clear/" + uuid, null);
		new ReceiveReception(method, this, call, req);
	}

	/**
	 * 格納されているデータ数を取得.
	 * 
	 * @param call
	 *            コールバック処理. 正常に処理された場合、格納されているデータ数が返却されます.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void size(ReceptionCallback call) throws IOException {
		String method = "size";
		if (uuid.length() == 0) {
			errorResult(method, call);
			return;
		}

		byte[] req = createHttpRequest("/size/" + uuid, null);
		new ReceiveReception(method, this, call, req);
	}

	/**
	 * データ送信.
	 * 
	 * @param data
	 *            送信対象のデータを設定します.
	 * @param call
	 *            コールバック処理. [true]が返却された場合、データがセットされました.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void send(String data, ReceptionCallback call) throws IOException {
		String method = "send";
		if (uuid.length() == 0) {
			errorResult(method, call);
			return;
		}

		byte[] req = createHttpRequest("/send/" + uuid, data);
		new ReceiveReception(method, this, call, req);
	}

	/**
	 * 直接データ送信.
	 * 
	 * @param uuid
	 *            対象のUUIDを設定します.
	 * @param data
	 *            送信対象のデータを設定します.
	 * @param call
	 *            コールバック処理. [true]が返却された場合、データがセットされました.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void connectSend(String uuid, String data, ReceptionCallback call)
			throws IOException {
		reconnect(uuid);
		send(data, call);
	}

	// HTTPリクエストを作成.
	protected final byte[] createHttpRequest(String url, String body)
			throws IOException {
		byte[] b = null;
		StringBuilder buf = new StringBuilder();
		if (body != null) {
			buf.append("POST ");
		} else {
			buf.append("GET ");
		}
		buf.append(url).append(" HTTP/1.1\r\n");
		buf.append("Host: ").append(address).append(":").append(port).append(
				"\r\n");
		buf.append("User-Agent: ").append(Def.RECEPTION_CLIENT_NAME).append(
				"\r\n");
		if (body != null) {
			b = body.getBytes("UTF8");
			buf.append("Content-Length: ").append(b.length).append("\r\n");
			buf.append("\r\n");
		} else {
			buf.append("\r\n");
		}

		// binary 変換.
		String s = buf.toString();
		buf = null;
		byte[] n = s.getBytes("UTF8");
		s = null;

		if (b == null) {
			return n;
		}

		byte[] ret = new byte[n.length + b.length];
		System.arraycopy(n, 0, ret, 0, n.length);
		System.arraycopy(b, 0, ret, n.length, b.length);
		return ret;
	}

	/**
	 * UUIDを取得.
	 * 
	 * @return String UUIDが返却されます.
	 */
	public String getUUID() {
		return uuid;
	}

	// エラー処理.
	protected static final void errorResult(String method,
			ReceptionCallback call) throws IOException {
		if ("connect".equals(method)) {
			call.onResult((Map) Json.decode("{\"uuid\":\"\"}"));
		} else if ("disconnect".equals(method)) {
			call.onResult((Map) Json.decode("{\"result\":false}"));
		} else if ("size".equals(method)) {
			call.onResult((Map) Json.decode("{\"result\":false,\"size\": -1}"));
		} else if ("send".equals(method)) {
			call.onResult((Map) Json.decode("{\"result\":false}"));
		}
	}

	/** Reception(受付処理)スレッド処理. **/
	private static final class ReceiveReception extends Thread {
		private Socket socket;
		private InputStream in;
		private OutputStream out;
		private ByteArrayIO buf;

		private String method = null;
		private byte[] sendData = null;
		private ReceptionClient client;
		private ReceptionCallback call;

		public ReceiveReception(String m, ReceptionClient c,
				ReceptionCallback cb, byte[] s) {
			this.method = m;
			this.client = c;
			this.call = cb;
			this.sendData = s;

			setDaemon(true);
			start();
		}

		public void run() {
			try {

				// 初期化処理.
				initConnect();

				// データ送信.
				send();

				// 受信処理.
				recv();

			} catch (Exception e) {
				try {
					errorResult(method, call);
				} catch (Exception ee) {
				}
			} finally {
				closeConnect();
			}

		}

		protected final void initConnect() throws IOException {
			try {
				this.socket = CreateSocket.create(client.ssl, client.address,
						client.port, client.timeout);
				this.in = new BufferedInputStream(socket.getInputStream());
				this.out = new BufferedOutputStream(socket.getOutputStream());
				this.buf = new ByteArrayIO();
			} catch (IOException e) {
				closeConnect();
				throw e;
			}
		}

		protected final void closeConnect() {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
				in = null;
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
				out = null;
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
				}
				socket = null;
			}
			buf = null;
		}

		protected final void send() throws IOException {
			out.write(sendData);
			out.flush();
			sendData = null;
		}

		private static final byte[] HTTP_EOF = ("\r\n\r\n").getBytes();

		protected final void recv() throws IOException {
			int p, len;
			int contentLength = -1;
			byte[] b;
			final byte[] binary = new byte[1024];

			while ((len = in.read(binary)) != -1) {
				buf.write(binary, 0, len);
				if (contentLength == -1) {
					p = buf.indexOf(HTTP_EOF);
					if (p == -1) {
						continue;
					}
					b = new byte[p + HTTP_EOF.length];
					buf.read(b);
					String header = new String(b, "UTF8");
					b = null;

					// 正常でない結果.
					if (!header.startsWith("HTTP/1.1 200")) {
						call.onResult((Map) Json
								.decode("{\"result\":false, \"status\":"
										+ header.substring(9, 12) + "}"));
						return;
					}
					// contentLengthが存在する場合.
					if (header.indexOf("\r\nContent-Length: ") != -1) {

						contentLength = getContentLength(header);
						if (buf.size() >= contentLength) {

							// bodyデータの受信が完了した場合.
							b = new byte[contentLength];
							buf.read(b);
							String result = new String(b, "UTF8");
							b = null;
							successCall(result);
							break;
						}
					} else {
						break;
					}

				} else if (buf.size() >= contentLength) {

					// bodyデータの受信が完了した場合.
					b = new byte[contentLength];
					buf.read(b);
					String result = new String(b, "UTF8");
					b = null;
					successCall(result);
					break;
				}
			}

		}

		protected final int getContentLength(String header) {
			int p = header.indexOf("Content-Length: ");
			int e = header.indexOf("\r\n", p);
			return Integer.parseInt(header.substring(p + 16, e));
		}

		protected final void successCall(String result) throws IOException {
			Map res = (Map) Json.decode(result);
			if ("connect".equals(method)) {
				call.onResult(res);
				client.uuid = (String) res.get("uuid");
			} else if ("disconnect".equals(method)) {
				call.onResult(res);
			} else if ("size".equals(method)) {
				call.onResult(res);
			} else if ("send".equals(method)) {
				call.onResult(res);
			}
		}
	}
}
