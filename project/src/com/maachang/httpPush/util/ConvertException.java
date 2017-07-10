package com.maachang.httpPush.util;

/**
 * 変換系例外.
 */
public class ConvertException extends RuntimeException {
	private static final long serialVersionUID = 2547034651824606342L;

	public ConvertException() {
		super();
	}

	public ConvertException(String m) {
		super(m);
	}

	public ConvertException(Throwable e) {
		super(e);
	}

	public ConvertException(String m, Throwable e) {
		super(m, e);
	}
}
