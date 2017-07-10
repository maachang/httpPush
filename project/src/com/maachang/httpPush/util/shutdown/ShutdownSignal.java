package com.maachang.httpPush.util.shutdown;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * シャットダウンシグナル送信.
 */
class ShutdownSignal {

	/**
	 * デフォルトポート番号.
	 */
	public static final int DEFAULT_PORT = 9998;

	/**
	 * ローカルアドレス.
	 */
	public static final String LOCAL_ADDRESS = "127.0.0.1";

	/**
	 * シャットダウン送信内容.
	 */
	public static final byte[] SHUTDOWN_BINARY = { (byte) 0x81, (byte) 0x19,
			(byte) 0x92, (byte) 0x88 };

	/**
	 * デフォルトタイムアウト.
	 */
	private static final int DEF_TIMEOUT = 5000;

	/**
	 * シャットダウン情報を送信. <BR>
	 * <BR>
	 * シャットダウン情報を送信します. <BR>
	 * 
	 * @param port
	 *            シャットダウン先のポート番号を設定します.
	 * @return boolean 停止完了が成立した場合[true]が返されます.
	 * @exception Exception
	 *                例外.
	 */
	public static boolean send(int port, int timeout) throws Exception {
		if (port <= 0 || port > 65535) {
			port = DEFAULT_PORT;
		}
		if (timeout <= 0 || timeout >= 30000) {
			timeout = DEF_TIMEOUT;
		}
		// 規定条件を送信.
		DatagramSocket s = new DatagramSocket();
		s.send(new DatagramPacket(SHUTDOWN_BINARY, 0, SHUTDOWN_BINARY.length,
				InetAddress.getByName(LOCAL_ADDRESS), port));
		boolean ret = true;
		// 停止完了を待つ.
		try {
			s.setSoTimeout(timeout);
			DatagramPacket p = new DatagramPacket(new byte[512], 512);
			s.receive(p);
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}
}
