package com.maachang.httpPush.net.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.maachang.httpPush.util.ByteArrayIO;

/**
 * Httpヘッダ情報. 基本HTTPヘッダ情報のみを保持します. (bodyデータは非保持).
 */
public class HttpHeader {
	protected String method;
	protected String url;
	protected String version;

	protected byte[] headers;
	protected String headersString;

	protected HttpHeader() {
	}

	public HttpHeader(ByteArrayIO buffer, int endPoint) throws IOException {
		int firstPoint = buffer.indexOf(HttpAnalysis.ONE_LINE);
		byte[] b = new byte[firstPoint];
		buffer.read(b);
		buffer.skip(HttpAnalysis.ONE_LINE.length);

		String v = new String(b, "UTF8");
		b = null;
		analysisFirst(v);
		v = null;

		int len = endPoint + HttpAnalysis.END_LINE.length
				- (firstPoint + HttpAnalysis.ONE_LINE.length);
		b = new byte[len];
		buffer.read(b);

		this.headers = b;
		this.headersString = null;
	}

	protected final void analysisFirst(String v) throws IOException {
		String[] list = v.split(" ");
		if (list.length != 3) {
			throw new IOException("受信データはHTTPリクエストではありません:" + v);
		}
		this.method = list[0];
		this.url = list[1];
		this.version = list[2];
	}

	public String getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public String getVersion() {
		return version;
	}

	protected final void convertString() throws IOException {
		if (headers != null) {
			headersString = new String(headers, "UTF8");
			headers = null;
		}
	}

	public String getHeader(String key) throws IOException {
		convertString();

		int p = headersString.indexOf(key + ": ");
		if (p == -1) {
			return null;
		}
		int end = headersString.indexOf("\r\n", p);
		if (end == -1) {
			return null;
		}
		return headersString.substring(p + key.length() + 2, end);
	}

	public List<String> getHeaders() throws IOException {
		convertString();

		int p;
		int b = 0;
		List<String> ret = new ArrayList<String>();
		while ((p = headersString.indexOf(": ", b)) != -1) {
			ret.add(headersString.substring(b, p));
			b = p + 2;
			p = headersString.indexOf("\r\n", b);
			if (p == -1) {
				break;
			}
			b = p + 2;
		}
		return ret;
	}
}
