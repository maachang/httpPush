package com.maachang.httpPush.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Save / Load Util.
 */
class SaveLoadUtil {
	protected SaveLoadUtil() {
	}

	public static final void saveInt(OutputStream o, int n) throws IOException {
		o.write(new byte[] { (byte) (n & 0x000000ff),
				(byte) ((n & 0x0000ff00) >> 8),
				(byte) ((n & 0x00ff0000) >> 16),
				(byte) ((n & 0xff000000) >> 24) });
	}

	public static final void saveString(OutputStream o, String n)
			throws IOException {
		n = (n == null) ? "" : n;
		byte[] b = n.getBytes("UTF8");
		saveInt(o, b.length);
		if (b.length > 0) {
			o.write(b);
		}
	}

	public static final int loadInt(InputStream n) throws IOException {
		byte[] b = new byte[4];
		n.read(b);
		return (b[0] & 0x000000ff) | ((b[1] & 0x000000ff) << 8)
				| ((b[2] & 0x000000ff) << 16) | ((b[3] & 0x000000ff) << 24);
	}

	public static final String loadString(InputStream n) throws IOException {
		int len = loadInt(n);
		if (len > 0) {
			byte[] b = new byte[len];
			n.read(b);
			return new String(b, "UTF8");
		}
		return "";
	}

}
