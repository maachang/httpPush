package com.maachang.httpPush.client;

import java.io.IOException;

/**
 * 1つのPushイベントデータを管理します.
 */
public final class SseData {

    /**
     * イベントのタイプです。これが指定されている場合、イベントはブラウザ内で、 イベント名に応じたイベントリスナへ送られます。Web
     * サイトのソースコードでは 名前付きイベントを受け取るために、addEventListener() を使用します。
     * メッセージでイベント名が指定されていない場合は、onmessage ハンドラが 呼び出されます。
     */
    protected String event;

    /**
     * EventSource オブジェクトの last event ID の値に設定する、イベント ID です。
     */
    protected String id;

    /**
     * イベントの送信を試みるときに使用する reconnection time です。 [What code handles this?]
     * これは整数値であることが必要で、 reconnection time をミリ秒単位で指定します。整数値ではない値が
     * 指定されると、このフィールドは無視されます。
     */
    protected String retry;

    /**
     * メッセージのデータフィールドです。EventSource が data: で始まる、
     * 複数の連続した行を受け取ったときは、それらを連結して各項目の間に 改行文字を挿入します。終端の改行は取り除かれます。
     */
    protected String data;

    protected long createTime;

    public SseData() {
        this(null, null, null, null);
    }

    public SseData(String v) {
        this(null, null, null, v);
    }

    public SseData(String e, String v) {
        this(e, null, null, v);
    }

    public SseData(String e, String r, String v) {
        this(e, r, null, v);
    }

    public SseData(String e, String r, String i, String v) {
        event = (e == null) ? "" : e;
        retry = (r == null) ? "" : r;
        id = (i == null) ? "" : i;
        data = (v == null) ? "" : v;
        createTime = System.currentTimeMillis();
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String e) {
        event = e;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRetry() {
        return retry;
    }

    public void setRetry(String retry) {
        this.retry = retry;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public byte[] toBinary() throws IOException {
        StringBuilder buf = new StringBuilder();
        if (data == null) {
            data = "";
        }
        buf.append("data: ").append(data).append("\n");
        if (!event.isEmpty()) {
            buf.append("event: ").append(event).append("\n");
        }
        if (!id.isEmpty()) {
            buf.append("id: ").append(id).append("\n");
        }
        if (!retry.isEmpty()) {
            buf.append("retry: ").append(retry).append("\n");
        }
        buf.append("\n");
        return buf.toString().getBytes("UTF8");
    }
}
