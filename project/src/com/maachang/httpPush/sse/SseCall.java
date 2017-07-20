package com.maachang.httpPush.sse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.maachang.httpPush.data.PushData;
import com.maachang.httpPush.data.PushDataManager;
import com.maachang.httpPush.net.NioCall;
import com.maachang.httpPush.net.NioElement;
import com.maachang.httpPush.net.SendData;
import com.maachang.httpPush.net.http.HttpAnalysis;
import com.maachang.httpPush.net.http.HttpHeader;
import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.util.ByteArrayIO;
import com.maachang.httpPush.util.Utils;
import com.maachang.httpPush.util.sequence.Time16SequenceId;

/**
 * Sseコール処理.
 */
public final class SseCall extends NioCall {

    /** LOG. **/
    private static final Log LOG = LogFactory.getLog(SseCall.class);
    private PushDataManager manager;

    /**
     * コンストラクタ.
     */
    public SseCall(PushDataManager m) {
        manager = m;
    }

    /**
     * 新しい通信要素を生成.
     * 
     * @return BaseNioElement 新しい通信要素が返却されます.
     */
    public NioElement createElement() {
        return new SseElement();
    }

    /**
     * 開始処理.
     * 
     * @return boolean [true]の場合、正常に処理されました.
     */
    public boolean startNio() {
        LOG.info("##### start SSE nio");
        return true;
    }

    /**
     * 終了処理.
     */
    public void endNio() {
        LOG.info("##### stop SSE nio");
    }

    /**
     * エラーハンドリング.
     */
    public void error(Throwable e) {
        LOG.error("##### error SSE nio", e);
    }

    /**
     * Accept処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean accept(NioElement em) throws IOException {
        LOG.debug("##### accept SSE nio");

        return true;
    }

    /**
     * Send処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @param buf
     *            対象のByteBufferを設定します.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean send(NioElement em, ByteBuffer buf) throws IOException {
        LOG.debug("##### send SSE nio");

        SseElement sem = (SseElement) em;

        // 送信データを取得.
        SendData sendData = sem.getSendData();
        byte[] b = sendData.get();

        // 送信対象のデータが存在しない場合.
        if (b == null) {

            // comet通信の場合、chunkedの終端を送付済みの場合は、Queueから
            // データを取得しない.
            if (sendData.isChunkedEnd()) {

                // データが存在しない場合.
                if (buf.position() == 0) {

                    // クローズ処理.
                    return false;
                }
                return true;
            }

            // 新しいデータが存在するかチェック.
            PushData data = manager.poll(sem.getUUID());
            if (data == null) {

                // データが存在しない場合.
                if (buf.position() == 0) {

                    // 送信処理が完了した場合は、OP_WRITE を停止.
                    sem.interestOps(SelectionKey.OP_READ);
                }
                return true;
            }

            // データが存在する場合は、新しくセット.
            sendData.set(data.toBinary());
            b = sendData.get();
        }

        // 送信処理.
        int p = sendData.getPosition();
        int n = b.length - p;
        int len = buf.limit() - buf.position();
        if (len > n) {
            buf.put(b, p, n);
            sendData.clear();
        } else {
            buf.put(b, p, len);
            sendData.setPosition(p + len);
        }
        return true;
    }

    /**
     * Receive処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @param buf
     *            対象のByteBufferを設定します.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean receive(NioElement em, ByteBuffer buf) throws IOException {
        LOG.debug("##### recv SSE nio:" + buf);

        SseElement sem = (SseElement) em;

        // 受信バッファに今回分の情報をセット.
        ByteArrayIO buffer = sem.getBuffer();
        buffer.write(buf);
        int endPoint = HttpAnalysis.endPoint(buffer);
        if (endPoint == -1) {

            // 受信途中の場合.
            return true;
        }
        HttpHeader header = HttpAnalysis.getHeader(buffer, endPoint);

        // 1度受信処理が完s了したら、受信バッファは利用しないので、削除.
        sem.destroyBuffer();

        // GET以外のアクセスの場合は処理しない.
        if (!"GET".equals(header.getMethod())) {
            return false;
        }
        String domain = null;
        String uuid = null;
        String protocol = null;

        List<String> params = getParams(header.getUrl());

        // SSEアクセスの場合.
        if (params.size() == 3
                || "text/event-stream".equals(header.getHeader("Accept"))) {
            domain = params.get(0);
            uuid = params.get(1);
            protocol = params.get(2);
            sem.setComet(false);
        }
        // cometアクセスの場合.
        else {
            uuid = params.get(0);
            sem.setComet(true);
        }

        // UUIDを取得し、UUIDの妥当性チェックを行う.
        if (uuid == null || Time16SequenceId.getBytes(uuid) == null) {

            // UUIDが存在しない、もしくは不正な値である場合は処理しない.
            return false;
        }

        // UUIDをセット.
        sem.setUUID(uuid);

        // managerに要素をセット.
        if (!manager.isUUID(uuid)) {
            manager.create(uuid);
        }
        manager.setElement(uuid, sem);

        // 返信データをセット.
        if (!sem.isComet()) {
            // SSE.
            sem.getSendData().set(getResponse(domain, protocol));
        } else {
            // Coment.
            sem.getSendData().set(COMMET_RESPONSE);
        }
        return true;
    }

    /** パラメータを取得. **/
    private static final List<String> getParams(String url) {
        int p = url.indexOf("?");
        if (p != -1) {
            url = url.substring(0, p);
        }
        List<String> ret = Utils.cutString(url, "/");
        if (ret.size() == 1) {
            return ret;
        }
        if (ret.size() == 2) {
            ret.add("http");
        }
        String protocol = ret.get(2);

        // jsで[window.location.protocol]を指定して、設定している場合.
        // 一番後ろの文字に:がセットされるので、これを除外する.
        if (protocol.endsWith(":")) {
            ret.set(2, protocol.substring(0, protocol.length() - 1));
        }
        return ret;
    }

    /** HTTPレスポンスを取得. **/
    private static final byte[] getResponse(String domain, String protocol)
            throws IOException {
        if (!("http".equals(protocol) || "https".equals(protocol))) {
            protocol = "https";
        }
        byte[] url = (protocol + "://" + domain).getBytes("UTF8");
        int len = url.length + SSE_RESPONSE_1.length + SSE_RESPONSE_2.length;
        int pos = 0;
        byte[] ret = new byte[len];
        System.arraycopy(SSE_RESPONSE_1, 0, ret, pos, SSE_RESPONSE_1.length);
        pos += SSE_RESPONSE_1.length;
        System.arraycopy(url, 0, ret, pos, url.length);
        pos += url.length;
        System.arraycopy(SSE_RESPONSE_2, 0, ret, pos, SSE_RESPONSE_2.length);
        return ret;
    }

    /** SSE レスポンス. **/
    private static final byte[] SSE_RESPONSE_1;
    private static final byte[] SSE_RESPONSE_2;
    private static final byte[] COMMET_RESPONSE;
    static {
        byte[] b, bb;
        byte[] c;
        try {
            // 空のデータを最初に１回だけ飛ばす.
            // jsの[sse.readyState]を1にするため.
            b = ("HTTP/1.1 200 OK\r\n" + "Content-Type: text/event-stream\r\n"
                    + "Cache-Control: no-cache\r\n" + "Connection: close\r\n"
                    + "X-Accel-Buffering: no\r\n"
                    + "Access-Control-Allow-Origin: ").getBytes("UTF8");
            bb = ("\r\n" + "Access-Control-Allow-Credentials: true\r\n"
                    + "Server: " + Def.SSE_SERVER_NAME + "\r\n\r\n" + "data:\n\n")
                    .getBytes("UTF8");

            // comet用.
            c = ("HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n"
                    + "Cache-Control: no-cache\r\n" + "Connection: close\r\n"
                    + "X-Accel-Buffering: no\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "Transfer-Encoding: chunked\r\n" + "Server: "
                    + Def.SSE_SERVER_NAME + "\r\n\r\n").getBytes("UTF8");
        } catch (Exception e) {
            b = null;
            bb = null;
            c = null;
        }
        SSE_RESPONSE_1 = b;
        SSE_RESPONSE_2 = bb;
        COMMET_RESPONSE = c;
    }
}
