package com.maachang.httpPush.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Pushデータ.
 */
public class PushData {
	public static final PushData create() throws IOException {
		return create(null, null, null, null);
	}

	public static final PushData create(String data) throws IOException {
		return create(null, null, null, data);
	}

	public static final PushData create(String event, String data)
			throws IOException {
		return create(event, null, null, data);
	}

	public static final PushData create(String event, String retry, String data)
			throws IOException {
		return create(event, retry, null, data);
	}

	public static final PushData create(String event, String retry, String id,
			String data) throws IOException {
		StringBuilder buf = new StringBuilder();
		if (data == null) {
			data = "";
		}
		buf.append("data: ").append(data).append("\n");
		if (event != null && !event.isEmpty()) {
			buf.append("event: ").append(event).append("\n");
		}
		if (id != null && !id.isEmpty()) {
			buf.append("id: ").append(id).append("\n");
		}
		if (retry != null && !retry.isEmpty()) {
			buf.append("retry: ").append(retry).append("\n");
		}
		buf.append("\n");
		return new PushData(buf.toString().getBytes("UTF8"));
	}

	private byte[] binary;
	private long createTime;

	public PushData(byte[] binary) {
		this.binary = binary;
		this.createTime = System.currentTimeMillis();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public byte[] toBinary() throws IOException {
		return binary;
	}

	protected void load(InputStream o) throws IOException {
		createTime = System.currentTimeMillis()
				- (long) SaveLoadUtil.loadInt(o);

		int len = SaveLoadUtil.loadInt(o);
		binary = new byte[len];
		o.read(binary, 0, len);
	}

	protected void save(OutputStream o) throws IOException {
		int time = (int) (System.currentTimeMillis() - createTime);

		SaveLoadUtil.saveInt(o, time);
		SaveLoadUtil.saveInt(o, binary.length);
		o.write(binary, 0, binary.length);
	}

}
