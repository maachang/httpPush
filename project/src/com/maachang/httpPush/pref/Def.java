package com.maachang.httpPush.pref;

/**
 * 基本定義.
 */
public final class Def {
	protected Def() {
	}

	public static final String VERSION = "1.0.0";

	public static final String SSE_CLIENT_NAME = "HttpPush Client(SSE: "
			+ VERSION + ")";
	public static final String SSE_SERVER_NAME = "HttpPush (SSE: " + VERSION
			+ ")";
	public static final String RECEPTION_CLIENT_NAME = "HttpPush Client(Reception: "
			+ VERSION + ")";
	public static final String RECEPTION_SERVER_NAME = "HttpPush (Reception: "
			+ VERSION + ")";

	public static final String HTTP_PUSH_DATA = "./.httpPushData";

	public static final String CONF_PATH = "./conf/";
	public static final String CONF_FILE = "httpPush.conf";
}
