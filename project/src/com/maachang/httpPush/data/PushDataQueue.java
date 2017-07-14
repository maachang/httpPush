package com.maachang.httpPush.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.maachang.httpPush.sse.SseElement;
import com.maachang.httpPush.util.atomic.AtomicNumber;

/**
 * Push用Queue. １つのPushイベント接続に対する、キューによるPushイベントデータを管理します.
 */
public final class PushDataQueue {
	protected Queue<PushData> queue = new ConcurrentLinkedQueue<PushData>();
	protected AtomicNumber lastAccessTime = new AtomicNumber(System
			.currentTimeMillis());
	protected SseElement element = null;

	public void clear() {
		queue.clear();
		if (element != null) {
			element.clear();
			element = null;
		}
	}

	public void offer(PushData data) {
		lastAccessTime.set(System.currentTimeMillis());
		queue.offer(data);
	}

	public PushData poll() {
		lastAccessTime.set(System.currentTimeMillis());
		return queue.poll();
	}

	public int size() {
		lastAccessTime.set(System.currentTimeMillis());
		return queue.size();
	}

	public void setElement(SseElement em) {
		lastAccessTime.set(System.currentTimeMillis());
		if (element != null) {
			element.clear();
		}
		element = em;
	}

	public SseElement getElement() {
		lastAccessTime.set(System.currentTimeMillis());
		return element;
	}

	// 最終利用時間を取得.
	public long getLastAccessTime() {
		return lastAccessTime.get();
	}

	// i/o時に対して、expireを行う.
	// expLen = expireチェックを行う最低サイズ.
	// expTime = expireのタイムアウト値（ミリ秒）
	protected void exp(int expLen, long expTime) {
		if (expLen >= 0 && expTime > 0L && queue.size() > expLen) {
			PushData d;
			long now = System.currentTimeMillis();
			Iterator<PushData> it = queue.iterator();
			while (it.hasNext()) {
				d = it.next();
				if (now > d.getCreateTime() + expTime) {
					it.remove();
					if (queue.size() <= expLen) {
						break;
					}
				}
			}
		}
	}

	protected void load(InputStream o) throws IOException {
		lastAccessTime.set(System.currentTimeMillis());
		int len = SaveLoadUtil.loadInt(o);
		if (len > 0) {
			PushData d;
			for (int i = 0; i < len; i++) {
				d = PushData.create();
				d.load(o);
				queue.offer(d);
			}
		}
	}

	protected void save(OutputStream o) throws IOException {
		PushData d;
		SaveLoadUtil.saveInt(o, queue.size());
		Iterator<PushData> it = queue.iterator();
		while (it.hasNext()) {
			d = it.next();
			d.save(o);
		}
	}
}
