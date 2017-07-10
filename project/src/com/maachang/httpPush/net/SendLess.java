package com.maachang.httpPush.net;

import java.nio.ByteBuffer;

/**
 * Nioでの送信処理で、送信したが、その情報のあまりが 存在する場合のあまり情報保持オブジェクト.
 */
public class SendLess {
	// private static final int MIN_LENGTH = 32;
	// private static final int MAX_LENGTH = 256;
	private byte[] binary = null;
	private int length = 0;

	public SendLess() {
	}

	public void clear() {
		binary = null;
		length = 0;
	}

	/**
	 * 送信のあまりが存在する場合、あまりを保持.
	 * 
	 * @param buf
	 *            送信処理後のByteBufferを設定します.
	 */
	public void evacuate(ByteBuffer buf) {
		int len = buf.remaining();
		if (len == 0) {
			return;
		}
		if (binary == null || binary.length < len) {
			// if(len < MIN_LENGTH) {
			// binary = new byte[MIN_LENGTH];
			// } else {
			// binary = new byte[len];
			// }
			binary = new byte[len];
		}
		buf.get(binary, 0, len);
		length = len;
	}

	/**
	 * 前回送信のあまり情報をByteBufferにセット.
	 * 
	 * @param buf
	 *            対象のByteBufferを設定します.
	 */
	public void setting(ByteBuffer buf) {
		if (length == 0) {
			return;
		}
		buf.put(binary, 0, length);
		// if(binary.length >= MAX_LENGTH) {
		binary = null;
		// }
		length = 0;
	}
}
