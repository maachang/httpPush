package com.maachang.httpPush.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 基本Nio処理. accept,read,writeのnioイベントを１つのスレッドで処理します。
 */
public class BaseNio extends Thread {
	private static final int SELECTOR_TIMEOUT = 1000;
	private int byteBufferLength;
	private int socketSendBuffer;
	private int socketRecvBuffer;
	private boolean keepAlive;
	private boolean tcpNoDeley;
	private ServerSocketChannel server;
	private NioCall call;

	private volatile boolean stopFlag = true;

	/** LOG. **/
	private static final Log LOG = LogFactory.getLog(BaseNio.class);

	/**
	 * コンストラクタ.
	 * 
	 * @param byteBufferLength
	 * @param socketSendBuffer
	 * @param socketRecvBuffer
	 * @param keepAlive
	 * @param tcpNoDeley
	 * @param server
	 * @param call
	 */
	public BaseNio(int byteBufferLength, int socketSendBuffer,
			int socketRecvBuffer, boolean keepAlive, boolean tcpNoDeley,
			ServerSocketChannel server, NioCall call) {
		this.byteBufferLength = byteBufferLength;
		this.socketSendBuffer = socketSendBuffer;
		this.socketRecvBuffer = socketRecvBuffer;
		this.keepAlive = keepAlive;
		this.tcpNoDeley = tcpNoDeley;
		this.server = server;
		this.call = call;
	}

	public void startThread() {
		stopFlag = false;
		setDaemon(true);
		start();
	}

	public void stopThread() {
		stopFlag = true;
	}

	public boolean isStopThread() {
		return stopFlag;
	}

	public void run() {
		NioSelector selector = null;
		ThreadDeath d = null;
		try {

			// [call] 開始処理.
			if (call.startNio()) {

				// Selectorの初期化.
				// ServerSocketChannelをSelectorに登録.
				try {
					selector = new NioSelector();
					selector.register(server, SelectionKey.OP_ACCEPT);
					d = executeThread(selector);
				} catch (Exception e) {
					LOG.debug("error", e);
					call.error(e);
				}
			}
		} finally {

			// セレクタクローズ.
			if (selector != null) {
				selector.close();
			}

			// サーバーソケットクローズ.
			try {
				server.close();
			} catch (Exception e) {
				LOG.debug("error", e);
			}

			// [call] 終了処理.
			call.endNio();
		}

		if (d != null) {
			throw d;
		}
	}

	/** 処理スレッド. **/
	private final ThreadDeath executeThread(final NioSelector selector) {
		final int OP_ACCEPT = SelectionKey.OP_ACCEPT;
		final int OP_READ = SelectionKey.OP_READ;
		final int OP_WRITE = SelectionKey.OP_WRITE;
		final int ssb = socketSendBuffer;
		final int srb = socketRecvBuffer;
		final boolean kpF = keepAlive;
		final boolean tnF = tcpNoDeley;

		final ByteBuffer buf = ByteBuffer.allocateDirect(byteBufferLength);
		final ServerSocketChannel sc = server;
		final NioCall cl = call;

		ThreadDeath ret = null;
		boolean endFlag = false;

		int ops;
		Iterator<SelectionKey> it;
		SelectionKey key = null;
		SocketChannel ch = null;
		NioElement em = null;
		SendLess sl = null;

		while (!endFlag && !stopFlag) {
			key = null;
			em = null;
			sl = null;
			try {

				while (!endFlag && !stopFlag) {
					key = null;
					em = null;
					sl = null;
					if (!selector.select(SELECTOR_TIMEOUT)) {
						continue;
					}
					it = selector.iterator();

					while (it.hasNext()) {
						key = null;
						em = null;
						sl = null;
						try {

							// 今回処理対象の内容を取得.
							key = it.next();
							it.remove();

							// 対象キーが存在しない場合は処理しない.
							if (key == null || !key.isValid()) {

								// 取得情報が無効な場合は、オブジェクトクローズ.
								if (key != null) {
									NioUtil.destroyKey(key);
								}
								continue;
							}

							// オプション処理を取得.
							ops = key.readyOps();

							// accept(ServerSocketに接続)が検知された場合.
							if ((ops & OP_ACCEPT) == OP_ACCEPT) {

								// accept失敗.
								if ((ch = sc.accept()) == null) {
									NioUtil.destroyKey(key);
									continue;
								}

								// ソケット初期化.
								if (NioUtil.initSocket(ch, ssb, srb, kpF, tnF)) {

									// 要素の登録.
									em = cl.createElement();
									em.registor(selector, ch, OP_READ);
									ops = key.readyOps();

									// [call] accept処理コール.
									if (!cl.accept(em)) {
										em.clear();
										continue;
									}
								} else {

									// ソケット初期化に失敗.
									NioUtil.destroyKey(key);
									continue;
								}
							}

							// (Socket)読み込み処理および書き込み処理.
							if ((ops & OP_WRITE) == OP_WRITE
									|| (ops & OP_READ) == OP_READ) {

								// 必要情報の取得に失敗
								if ((em = (NioElement) key.attachment()) == null) {
									if (key != null) {
										NioUtil.destroyKey(key);
									}
									continue;
								}

								// ソケットチャネルを取得.
								ch = (SocketChannel) key.channel();

								// 書き込み可能処理.
								if ((ops & OP_WRITE) == OP_WRITE) {

									// 送信前処理.
									buf.clear();
									sl = em.getSendLess();
									sl.setting(buf);

									// [call] 送信処理.
									if (!cl.send(em, buf)) {
										em.clear();
										buf.clear();
										continue;
									}

									// 書き込み処理後.
									buf.flip();
									if (buf.remaining() > 0) {

										// 書き込み対象のデータが存在する場合.
										if (ch.write(buf) < 0) {

											// 通信エラー.
											em.clear();
											continue;
										}
										// 送信あまりがある場合.
										sl.evacuate(buf);
									}

									// バッファ初期化.
									buf.clear();
								}

								// 読み込み可能処理.
								if ((ops & OP_READ) == OP_READ) {

									// 受信処理.
									buf.clear();
									if (ch.read(buf) < 0) {
										// 通信エラー.
										em.clear();
										continue;
									}
									buf.flip();

									// [call] 受信処理.
									if (!cl.receive(em, buf)) {
										em.clear();
										buf.clear();
										continue;
									}
									buf.clear();
								}
							}

						} catch (IOException e) {
							LOG.debug("error", e);
							if (key != null) {
								NioUtil.destroyKey(key);
								key = null;
							}
							if (em != null) {
								em.clear();
								em = null;
							}
						}
					}
				}
			} catch (Throwable to) {
				LOG.debug("error", to);
				if (key != null) {
					NioUtil.destroyKey(key);
					key = null;
				}
				if (em != null) {
					em.clear();
					em = null;
				}
				if (to instanceof InterruptedException) {
					endFlag = true;
				} else if (to instanceof ThreadDeath) {
					endFlag = true;
					ret = (ThreadDeath) to;
				}
			}
		}
		return ret;
	}
}
