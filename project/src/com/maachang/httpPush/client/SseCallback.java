package com.maachang.httpPush.client;

/**
 * Sse受信コールバック.
 */
public abstract class SseCallback {

	/**
	 * HttpPushデータ受信時に呼び出されます.
	 * 
	 * @param data
	 *            対象のデータが設定されます.
	 */
	public abstract void onMessage(SseData data);

	/**
	 * HttpPushエラー発生時に呼び出されます.
	 * 
	 * @param message
	 *            エラー内容が設定されます.
	 * @param e
	 *            エラー詳細が設定されます.
	 */
	public void onError(String message, Throwable e) {

	}
}
