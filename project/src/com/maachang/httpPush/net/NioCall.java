package com.maachang.httpPush.net;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Nioコールバック.
 */
public abstract class NioCall {

	/**
	 * 新しい通信要素を生成.
	 * 
	 * @return BaseNioElement 新しい通信要素が返却されます.
	 */
	public abstract NioElement createElement();

	/**
	 * 開始処理.
	 * 
	 * @return boolean [true]の場合、正常に処理されました.
	 */
	public boolean startNio() {
		return true;
	}

	/**
	 * 終了処理.
	 */
	public void endNio() {

	}

	/**
	 * エラーハンドリング.
	 */
	public void error(Throwable e) {

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
		return true;
	}
}
