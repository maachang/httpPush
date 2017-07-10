package com.maachang.httpPush.sse;

/**
 * Sseサーバ設定.
 */
public class SseInfo {

	/** HTTP同時接続数. **/
	private int backlog = Integer.MAX_VALUE;

	/** Nioバッファ長. **/
	private int byteBufferLength = 1024;

	/** ソケット送信バッファ長. **/
	private int socketSendBuffer = 1024;

	/** ソケット受信バッファ長. **/
	private int socketReceiveBuffer = 2048;

	/** サーバーバインドアドレス. **/
	private String localAddress = null;

	/** サーバーバインドポート. **/
	private int localPort = 3332;

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getByteBufferLength() {
		return byteBufferLength;
	}

	public void setByteBufferLength(int byteBufferLength) {
		this.byteBufferLength = byteBufferLength;
	}

	public int getSocketSendBuffer() {
		return socketSendBuffer;
	}

	public void setSocketSendBuffer(int socketSendBuffer) {
		this.socketSendBuffer = socketSendBuffer;
	}

	public int getSocketReceiveBuffer() {
		return socketReceiveBuffer;
	}

	public void setSocketReceiveBuffer(int socketReceiveBuffer) {
		this.socketReceiveBuffer = socketReceiveBuffer;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
}
