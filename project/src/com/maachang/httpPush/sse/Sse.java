package com.maachang.httpPush.sse;

import java.nio.channels.ServerSocketChannel;

import com.maachang.httpPush.data.PushDataManager;
import com.maachang.httpPush.net.BaseNio;
import com.maachang.httpPush.net.NioUtil;

/**
 * ServerSentEvents送信側処理. MethodはGETのみ受付。
 */
public final class Sse {
	// net設定.
	protected static final boolean TCP_NO_DELAY = false; // Nagle アルゴリズムを有効にします.
	protected static final boolean KEEP_ALIVE = false; // TCP-KeepAliveを無効に設定します.

	/** Nio処理. **/
	private BaseNio nio = null;

	/**
	 * コンストラクタ.
	 * 
	 * @param info
	 * @param manager
	 * @throws Exception
	 */
	public Sse(SseInfo info, PushDataManager manager) throws Exception {

		// nio:サーバーソケット作成.
		ServerSocketChannel ch = NioUtil.createServerSocketChannel(info
				.getSocketReceiveBuffer(), info.getLocalAddress(), info
				.getLocalPort(), info.getBacklog());

		// nio処理を生成.
		this.nio = new BaseNio(info.getByteBufferLength(), info
				.getSocketSendBuffer(), info.getSocketReceiveBuffer(),
				KEEP_ALIVE, TCP_NO_DELAY, ch, new SseCall(manager));
	}

	public void start() {
		nio.startThread();
	}

	public void stop() {
		nio.stopThread();
	}

	public boolean isStop() {
		return nio.isStopThread();
	}
}
