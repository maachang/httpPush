package com.maachang.httpPush.data;

/**
 * UUIDが指定されていない場合の例外.
 */
public class NoUUIDException extends RuntimeException {
    private static final long serialVersionUID = 3962303103282874875L;

    public NoUUIDException() {
        super();
    }

    public NoUUIDException(String msg) {
        super(msg);
    }

    public NoUUIDException(Throwable e) {
        super(e);
    }

    public NoUUIDException(String msg, Throwable e) {
        super(msg, e);
    }

}
