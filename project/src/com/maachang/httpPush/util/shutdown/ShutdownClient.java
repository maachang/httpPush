package com.maachang.httpPush.util.shutdown;

/**
 * シャットダウンを通知するクライアント.
 * 
 * @version 2013/12/22
 * @author masahito suzuki
 * @since fshttp-1.00
 */
public class ShutdownClient {

    /**
     * １回のシャットダウン待ち.
     */
    private static final int TIMEOUT = 5000;

    /**
     * シャットダウンリトライ数.
     */
    private static final int RETRY = 12;

    /**
     * コンストラクタ.
     */
    private ShutdownClient() {

    }

    /**
     * シャットダウン通知.
     * 
     * @exception Exception
     *                例外.
     */
    public static final boolean send() throws Exception {
        return send(-1);
    }

    /**
     * シャットダウン通知.
     * 
     * @param port
     *            対象のポート番号を設定します.
     * @exception Exception
     *                例外.
     */
    public static final boolean send(int port) throws Exception {
        if (port <= 0 || port > 65535) {
            port = ShutdownSignal.DEFAULT_PORT;
        }
        for (int i = 0; i < RETRY; i++) {
            if (ShutdownSignal.send(port, TIMEOUT)) {
                return true;
            }
        }
        return false;
    }

}
