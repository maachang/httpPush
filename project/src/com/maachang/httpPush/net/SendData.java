package com.maachang.httpPush.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 送信データ.
 */
public class SendData {
    protected NioElement element = null;

    protected int chunkedState = 0;
    protected boolean chunked = false;
    protected int sendDataPosition = 0;
    protected byte[] sendData = null;

    public SendData(NioElement em) {
        element = em;
    }

    public void clear() {
        sendData = null;
        sendDataPosition = 0;
    }

    private static final byte[] CHUNKED_FOOTER;
    static {
        byte[] b = null;
        try {
            b = "0\r\n\r\n".getBytes("UTF8");
        } catch (Exception e) {
            b = null;
        }
        CHUNKED_FOOTER = b;
    }

    public void set(byte[] sendData) throws IOException {

        // データがチャンク送信の場合.
        if (chunkedState == 2) {
            byte[] h = (Integer.toHexString(sendData.length).toLowerCase() + "\r\n")
                    .getBytes("UTF8");
            int len = h.length + sendData.length + 2 + CHUNKED_FOOTER.length;
            byte[] b = new byte[len];
            int p = 0;
            
            // (length)\r\n
            System.arraycopy(h, 0, b, p, h.length);
            p += h.length;
            
            // body(length)
            System.arraycopy(sendData, 0, b, p, sendData.length);
            p += sendData.length;
            
            // \r\n
            b[p++] = (byte)'\r';
            b[p++] = (byte)'\n';
            
            // 0\r\n\r\n
            System.arraycopy(CHUNKED_FOOTER, 0, b, p, CHUNKED_FOOTER.length);
            sendData = b;
            chunkedState = 3;
        } else if (chunkedState == 1) {
            chunkedState = 2;
        }
        this.sendData = sendData;
        this.sendDataPosition = 0;
        element.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void setPosition(int pos) {
        sendDataPosition = pos;
    }

    public byte[] get() {
        return sendData;
    }

    public int getPosition() {
        return sendDataPosition;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
        this.chunkedState = chunked ? 1 : 0;
    }

    public boolean isChunked() {
        return chunked;
    }

    public boolean isChunkedEnd() {
        return chunkedState == 3;
    }
}
