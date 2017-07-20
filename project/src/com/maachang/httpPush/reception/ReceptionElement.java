package com.maachang.httpPush.reception;

import com.maachang.httpPush.net.NioElement;
import com.maachang.httpPush.net.http.HttpRequest;

/**
 * Reception(受付)要素.
 */
public final class ReceptionElement extends NioElement {
    protected String uuid;
    protected HttpRequest request;

    public void clear() {
        super.clear();
        uuid = null;
        request = null;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
