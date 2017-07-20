package com.maachang.httpPush.util.shutdown;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * サーバーシャットダウン監視処理. ShutdownClientから、シャットダウン通知が来るまで待機します.
 */
public class WaitShutdown {

    /**
     * 受信タイムアウト待ち.
     */
    private static final int RECEIVE_TIMEOUT = 1500;

    /**
     * 受信バッファ.
     */
    private byte[] recvBuffer = new byte[512];

    /**
     * シャットダウン待ちコネクション.
     */
    private DatagramSocket connection = null;

    /**
     * シャットダウン受信元ポート.
     */
    private int srcPort = -1;

    /**
     * コンストラクタ.
     */
    protected WaitShutdown() {

    }

    /**
     * コンストラクタ. <BR>
     * <BR>
     * シャットダウン待ちオブジェクトを生成します. <BR>
     * 
     * @param port
     *            対象のポート番号を設定します.
     * @exception Exception
     *                例外.
     */
    public WaitShutdown(int port) throws Exception {
        if (port <= 0 || port > 65535) {
            port = ShutdownSignal.DEFAULT_PORT;
        }
        connection = new DatagramSocket(port,
                InetAddress.getByName(ShutdownSignal.LOCAL_ADDRESS));
        connection.setSoTimeout(RECEIVE_TIMEOUT);
    }

    /**
     * シャットダウンウェイト. ※この処理を実行した場合、外部からのサーバー停止命令が来るまで 処理を待機します.
     * 
     * @param port
     *            対象のコマンド待ちポート番号を設定します.
     * @param exitCode
     *            正常なJavaプロセス終了のコードを設定します.
     * @exception Exception
     *                例外.
     */
    public static final void waitSignal(int port, int exitCode)
            throws Exception {
        WaitShutdown w = new WaitShutdown(port);
        while (true) {
            try {
                // シャットダウンシグナル待ち.
                if (w.isShutdown()) {
                    // シャットダウン返信.
                    w.exitShutdown();
                    System.exit(exitCode);
                }
            } catch (Throwable e) {
            }
        }
    }

    /**
     * シャットダウン待ち.
     * 
     * @return boolean [true]の場合、シャットダウンを受け付けました.
     */
    public boolean isShutdown() {
        boolean ret = false;
        try {
            DatagramPacket packet = new DatagramPacket(recvBuffer,
                    recvBuffer.length);
            connection.receive(packet);
            if (packet.getLength() == ShutdownSignal.SHUTDOWN_BINARY.length) {
                if (equals(packet) == true) {
                    srcPort = packet.getPort();
                    ret = true;
                }
            }
        } catch (Throwable e) {
            ret = false;
        }
        return ret;
    }

    /**
     * シャットダウン完了通知.
     */
    public void exitShutdown() {
        if (srcPort > 0) {
            try {
                connection.send(new DatagramPacket(
                        ShutdownSignal.SHUTDOWN_BINARY, 0,
                        ShutdownSignal.SHUTDOWN_BINARY.length, InetAddress
                                .getByName(ShutdownSignal.LOCAL_ADDRESS),
                        srcPort));
            } catch (Exception e) {
            }
        }
    }

    /**
     * 受信データが規定条件と一致する場合.
     */
    private static final boolean equals(DatagramPacket packet) {
        if (packet.getLength() == ShutdownSignal.SHUTDOWN_BINARY.length) {
            boolean ret = true;
            int len = packet.getLength();
            byte[] bin = packet.getData();
            for (int i = 0; i < len; i++) {
                if ((ShutdownSignal.SHUTDOWN_BINARY[i] & 0x000000ff) != (bin[i] & 0x000000ff)) {
                    ret = false;
                    break;
                }
            }
            return ret;
        }
        return false;
    }
}
