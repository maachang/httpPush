package com.maachang.httpPush.net.http;

import java.io.IOException;

import com.maachang.httpPush.util.ByteArrayIO;

/**
 * HttpRequest.
 */
public class HttpRequest extends HttpHeader {
    protected byte[] body = null;
    protected Integer contentLength = null;

    protected HttpRequest() {
        super();
    }

    public HttpRequest(ByteArrayIO buffer, int endPoint) throws IOException {
        super(buffer, endPoint);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public int getContentLength() throws IOException {
        if (contentLength != null) {
            return contentLength;
        }
        String ret = this.getHeader("Content-Length");
        if (ret == null) {
            return -1;
        }
        contentLength = Integer.parseInt(ret);
        return contentLength;
    }
}
