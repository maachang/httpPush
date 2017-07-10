package com.maachang.httpPush.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 送信データ.
 */
public class SendData {
	protected NioElement element = null;

	protected int sendDataPosition = 0;
	protected byte[] sendData = null;

	public SendData(NioElement em) {
		element = em;
	}

	public void clear() {
		sendData = null;
		sendDataPosition = 0;
	}

	public void set(byte[] sendData) throws IOException {
		this.sendData = sendData;
		this.sendDataPosition = 0;
		element.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	public void setPosition(int pos) {
		sendDataPosition = pos;
	}

	public byte[] get() {
		return sendData;
	}

	public int getPosition() {
		return sendDataPosition;
	}
}
