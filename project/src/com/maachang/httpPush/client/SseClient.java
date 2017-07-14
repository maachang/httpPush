package com.maachang.httpPush.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.util.ByteArrayIO;
import com.maachang.httpPush.util.atomic.AtomicNumber32;

/**
 * HttpPush Sse(ServerSentEvent)Client.
 */
public class SseClient {
	protected static final int DEFAULT_TIMEOUT = 5000;
	protected String uuid;
	protected boolean ssl;
	protected String address;
	protected int port;
	protected int timeout;

	protected final AtomicNumber32 closeFlag = new AtomicNumber32(1);
	protected ReceiveSSE receiveSse = null;

	/**
	 * コンストラクタ.
	 * 
	 * @param address
	 *            接続先アドレス(domain)を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public SseClient(String address) throws IOException {
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
	public SseClient(boolean ssl, String address) throws IOException {
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
	public SseClient(String address, int port) throws IOException {
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
	public SseClient(boolean ssl, String address, int port) throws IOException {
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
	public SseClient(String address, int port, int timeout) throws IOException {
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
	public SseClient(boolean ssl, String address, int port, int timeout)
			throws IOException {
		this.ssl = ssl;
		this.address = address;
		this.port = port;
		this.timeout = timeout;
	}

	/** デフォルトポート. **/
	private static final int defaultPort(boolean ssl) {
		return ssl ? 3442 : 3332;
	}

	/**
	 * 切断.
	 */
	public void close() {
		closeFlag.set(1);
		uuid = null;
		if (receiveSse != null) {
			receiveSse.stopThread();
			receiveSse = null;
		}
	}

	/**
	 * 接続処理.
	 * 
	 * @param uuid
	 *            対象のUUIDを設定します.
	 * @param call
	 *            Http受信コールバック処理を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void connect(String uuid, SseCallback call) throws IOException {
		this.closeFlag.set(0);
		this.uuid = uuid;
		this.receiveSse = new ReceiveSSE(this, call);
		this.receiveSse.startThread();
	}

	/**
	 * UUIDを取得.
	 * 
	 * @return String UUIDが返却されます.
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * 接続中かチェック.
	 * 
	 * @return boolean [true]の場合、接続中です.
	 */
	public boolean isConnect() {
		return closeFlag.get() == 0;
	}

	/** SSE(ServerSentEvent)受信待ちスレッド処理. **/
	private static final class ReceiveSSE extends Thread {
		private Socket socket;
		private InputStream in;
		private OutputStream out;
		private ByteArrayIO buf;

		private SseClient client;
		private SseCallback call;
		private volatile boolean stopFlag = true;

		public ReceiveSSE(SseClient c, SseCallback cb) {
			this.client = c;
			this.call = cb;
		}

		public void startThread() {
			this.stopFlag = false;
			setDaemon(true);
			start();
		}

		public void stopThread() {
			this.stopFlag = true;
		}

		public void run() {
			final byte[] binary = new byte[1024];
			ThreadDeath d = null;
			boolean endFlag = false;
			try {
				initConnect();
				while (!endFlag && !stopFlag) {
					try {
						execute(binary);
					} catch (Throwable e) {
						if (e instanceof InterruptedException) {
							endFlag = true;
						} else if (e instanceof ThreadDeath) {
							endFlag = true;
							d = (ThreadDeath) e;
						}
					}
				}
			} catch (Throwable t) {
				call.onError("error", t);
			}
			closeConnect();
			client = null;
			call = null;
			if (d != null) {
				throw d;
			}
		}

		// SSE通信初期化処理.
		// この処理はReceiveSSEスレッド内で呼ばれる.
		protected final void initConnect() throws IOException {
			try {
				this.socket = CreateSocket.create(client.ssl, client.address,
						client.port, client.timeout);
				this.in = new BufferedInputStream(socket.getInputStream());
				this.out = new BufferedOutputStream(socket.getOutputStream());
				this.buf = new ByteArrayIO();

				// リクエスト送信.
				sendRequest(client.uuid);

				// データ受信.
				receiveResponse();
			} catch (IOException e) {
				closeConnect();
				throw e;
			}
		}

		// 通信クローズ.
		// この処理はReceiveSSEスレッド内で呼ばれる.
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

		// リクエスト送信.
		private final void sendRequest(String uuid) throws IOException {
			final byte[] req = (new StringBuilder("GET /").append(
					client.address).append(":").append(client.port).append("/")
					.append(uuid).append("/").append(
							client.ssl ? "https" : "http").append(
							" HTTP/1.1\r\n").append("Host: ").append(
							client.address).append(":").append(client.port)
					.append("\r\n").append("Accept: text/event-stream").append(
							"\r\n").append("User-Agent: ").append(
							Def.SSE_CLIENT_NAME).append("\r\n\r\n").toString())
					.getBytes("UTF8");

			// データ書き込み.
			out.write(req);
			out.flush();
		}

		private static final byte[] HTTP_EOF = ("\r\n\r\n").getBytes();

		// レスポンス受信.
		private final void receiveResponse() throws IOException {
			int p;
			int len;
			byte[] binary = new byte[1024];
			try {
				while ((len = in.read(binary)) != -1) {
					buf.write(binary, 0, len);
					p = buf.indexOf(HTTP_EOF);
					if (p == -1) {
						continue;
					}
					byte[] b = new byte[p + HTTP_EOF.length];
					buf.read(b);
					String header = new String(b, "UTF8");
					b = null;

					// ヘッダ内容が異常な場合.
					if ((!header.startsWith("HTTP/1.1 200") && !header
							.startsWith("HTTP/1.0 200"))
							|| header
									.indexOf("\r\nContent-Type: text/event-stream\r\n") == -1) {
						throw new IOException("error response:\n" + header);
					}
					break;
				}
			} catch (IOException io) {
				client.close();
				throw io;
			}
		}

		/** SSEのデータ終端. **/
		private static final byte[] SSE_EOF = ("\n\n").getBytes();

		// 受信実行.
		private final void execute(final byte[] binary) throws IOException {
			final ByteArrayIO buf = this.buf;
			final InputStream in = this.in;

			int p, len;
			try {
				// キャッシュデータにデータが残っている場合.
				if ((p = buf.indexOf(SSE_EOF)) == -1) {
					// データ受信.
					if ((len = in.read(binary)) == -1) {

						// 切断された場合は、クライアントをクローズ.
						client.close();
						return;
					}
					buf.write(binary, 0, len);

					// SSEのデータ終端を取得.
					p = buf.indexOf(SSE_EOF);
				}
				if (p != -1) {
					byte[] value = new byte[p + SSE_EOF.length];
					buf.read(value);
					String data = new String(value, "UTF8");
					value = null;

					// コールバックにセット.
					setCall(data);
				}

			} catch (SocketTimeoutException st) {
				// タイムアウトは無視.
			}
		}

		// 受信データをCallbackにセット.
		private final void setCall(String data) throws IOException {

			// 受信データを解析.
			SseData pushData = new SseData();
			int p;
			int b = 0;
			String key;
			while ((p = data.indexOf(": ", b)) != -1) {
				key = data.substring(b, p);
				b = p + 2;
				p = data.indexOf("\n", b);
				if (p == -1) {
					break;
				}
				if ("data".equals(key)) {
					pushData.setData(data.substring(b, p));
				} else if ("event".equals(key)) {
					pushData.setEvent(data.substring(b, p));
				} else if ("id".equals(key)) {
					pushData.setId(data.substring(b, p));
				} else if ("retry".equals(key)) {
					pushData.setRetry(data.substring(b, p));
				}
				b = p + 2;
			}
			if (pushData.getData() == null || pushData.getData().length() == 0) {
				return;
			}
			call.onMessage(pushData);
		}
	}
}
