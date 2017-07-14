package com.maachang.httpPush.sse;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.maachang.httpPush.net.NioElement;

/**
 * Sse要素.
 */
public final class SseElement extends NioElement {
	protected String uuid = null;

	public void clear() {
		super.clear();
		uuid = null;
	}

	/**
	 * 受信バッファを破棄. ServerSentEventの場合、受信Onlyなので、最初にHTTP受信した後に削除する.
	 */
	public void destroyBuffer() {
		super.buffer = null;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	public void setComet(boolean comet) {
		sendData.setChunkedMode(comet);
	}

	public boolean isComet() {
		return sendData.isChunkedMode();
	}

	public void sendMode() throws IOException {
		super.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}
}
