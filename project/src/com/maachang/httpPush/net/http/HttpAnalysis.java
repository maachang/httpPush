package com.maachang.httpPush.net.http;

import java.io.IOException;

import com.maachang.httpPush.util.ByteArrayIO;
import com.maachang.httpPush.util.ListMap;
import com.maachang.httpPush.util.Utils;

/**
 * Httpリクエスト解析.
 */
public final class HttpAnalysis {
    protected HttpAnalysis() {
    }

    public static final byte[] ONE_LINE = "\r\n".getBytes();
    public static final byte[] END_LINE = "\r\n\r\n".getBytes();

    /**
     * Httpヘッダの終端が存在するかチェック.
     * 
     * @param buffer
     */
    public static final int endPoint(ByteArrayIO buffer) {
        return buffer.indexOf(END_LINE);
    }

    /**
     * HttpHeaderオブジェクトの生成.
     * 
     * @param buffer
     * @param endPoint
     * @return HttpHeader
     * @exception IOException
     */
    public static final HttpHeader getHeader(ByteArrayIO buffer, int endPoint)
            throws IOException {
        return new HttpHeader(buffer, endPoint);
    }

    /**
     * HttpRequestオブジェクトの生成.
     * 
     * @param buffer
     * @param endPoint
     * @return HttpRequest
     * @exception IOException
     */
    public static final HttpRequest getRequest(ByteArrayIO buffer, int endPoint)
            throws IOException {
        return new HttpRequest(buffer, endPoint);
    }

    /**
     * パラメータ変換処理. POSTのデータおよび、GETのデータを解析します.
     * 
     * @param body
     *            対象のBody情報を設定します.
     * @param cset
     *            対象のキャラクタセットを設定します.
     * @param pos
     *            対象のポジションを設定します.
     * @return ListMap 変換結果を返却します.
     * @exception IOException
     *                IO例外.
     */
    public static final ListMap paramsAnalysis(String body, int pos)
            throws IOException {
        return paramsAnalysis(body, "UTF8", pos);
    }

    /**
     * パラメータ変換処理. POSTのデータおよび、GETのデータを解析します.
     * 
     * @param body
     *            対象のBody情報を設定します.
     * @param cset
     *            対象のキャラクタセットを設定します.
     * @param pos
     *            対象のポジションを設定します.
     * @return ListMap 変換結果を返却します.
     * @exception IOException
     *                IO例外.
     */
    public static final ListMap paramsAnalysis(String body, String cset, int pos)
            throws IOException {
        // パラメータバイナリを解析.
        int p, n;
        String k;
        int b = pos;
        ListMap ret = new ListMap();
        while (true) {
            n = body.indexOf("&", b);
            if (n == -1) {
                k = body.substring(b);
                if ((p = k.indexOf("=")) == -1) {
                    break;
                }
                if (k.indexOf("%") != -1) {
                    ret.put(Utils.urlDecode(k.substring(0, p), cset),
                            Utils.urlDecode(k.substring(p + 1), cset));
                } else {
                    ret.put(k.substring(0, p), k.substring(p + 1));
                }
                break;
            }
            k = body.substring(b, n);
            if ((p = k.indexOf("=")) == -1) {
                b = n + 1;
                continue;
            }
            if (k.indexOf("%") != -1) {
                ret.put(Utils.urlDecode(k.substring(0, p), cset),
                        Utils.urlDecode(k.substring(p + 1), cset));
            } else {
                ret.put(k.substring(0, p), k.substring(p + 1));
            }
            b = n + 1;
        }
        return ret;
    }
}
