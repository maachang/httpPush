package com.maachang.httpPush.data;

/**
 * 停止通知例外.
 */
public class StopException extends RuntimeException {
	private static final long serialVersionUID = 6972877297791753276L;

	public StopException() {
		super();
	}

	public StopException(String m) {
		super(m);
	}

	public StopException(Throwable e) {
		super(e);
	}

	public StopException(String m, Throwable e) {
		super(m, e);
	}
}
