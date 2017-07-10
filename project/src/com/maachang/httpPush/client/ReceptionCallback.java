package com.maachang.httpPush.client;

import java.util.Map;

/**
 * Reception(受付処理)コールバック.
 */
public abstract class ReceptionCallback {

	/**
	 * コールバック.
	 * 
	 * @param result
	 *            処理結果のJSON情報が渡されます.
	 */
	public abstract void onResult(Map<String, Object> result);
}
