package com.maachang.httpPush.util.shutdown;

/**
 * シャットダウンフック.
 */
public class ShutdownHook extends Thread {
    /**
     * シャットダウンコールバックメソッド.
     */
    private CallbackShutdown callback = null;

    /**
     * シャットダウンフックの開始.
     * 
     * @param callback
     *            シャットダウンコールバックオブジェクトを設定します.
     * @return ShutdownHook 生成されたシャットダウンフックオブジェクトが返されます.
     * @exception Exception
     *                例外.
     */
    public static final ShutdownHook registHook(CallbackShutdown callback)
            throws Exception {
        if (callback == null) {
            throw new IllegalArgumentException("引数は不正です");
        }
        ShutdownHook ret = new ShutdownHook(callback);
        Runtime.getRuntime().addShutdownHook(ret);
        return ret;
    }

    /**
     * コンストラクタ.
     */
    private ShutdownHook() {

    }

    /**
     * コンストラクタ.
     * 
     * @param callback
     *            シャットダウンコールバックオブジェクトを設定します.
     */
    private ShutdownHook(CallbackShutdown callback) {
        super();
        this.callback = callback;
        this.setPriority(Thread.MAX_PRIORITY);
        this.setDaemon(false);
    }

    /**
     * シャットダウンコールバックオブジェクトを取得.
     * 
     * @return ShutdownCallback シャットダウンコールバックオブジェクトが返されます.
     */
    public CallbackShutdown getShutdownCallback() {
        return callback;
    }

    /**
     * シャットダウン実行.
     */
    public void run() {
        if (!callback.isShutdown()) {
            callback.execution();
        }
    }
}
