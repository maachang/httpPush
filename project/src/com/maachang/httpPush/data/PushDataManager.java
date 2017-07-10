package com.maachang.httpPush.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.maachang.httpPush.sse.SseElement;
import com.maachang.httpPush.util.atomic.AtomicNumber32;
import com.maachang.httpPush.util.sequence.Time16SequenceId;

/**
 * 全接続のPushデータを管理します.
 */
public class PushDataManager {
	protected final AtomicNumber32 stopFlag = new AtomicNumber32(0);
	protected Time16SequenceId seqManager;
	protected PushDataMonitor monitor;
	protected int expLen;
	protected long expTime;
	protected long closeTimeout;

	protected Map<String, PushDataQueue> manager = new ConcurrentHashMap<String, PushDataQueue>();

	protected PushDataManager() {
	}

	public PushDataManager(Time16SequenceId seq, int len, long time, long ctime) {
		seqManager = seq;
		expLen = len;
		expTime = time;
		closeTimeout = ctime;

		monitor = new PushDataMonitor(this);
		monitor.startThread();
	}

	public String createId() {
		return seqManager.getUUID();
	}

	public PushDataQueue create(String uuid) {
		PushDataQueue ret = new PushDataQueue();
		if (create(uuid, ret)) {
			return ret;
		}
		return null;
	}

	private void stopCheck() {
		if (stopFlag.get() != 0) {
			throw new StopException();
		}
	}

	public boolean create(String uuid, PushDataQueue value) {
		stopCheck();
		if (manager.containsKey(uuid)) {
			return false;
		}
		manager.put(uuid, value);
		return true;
	}

	public boolean clear(String uuid) {
		stopCheck();
		if (!manager.containsKey(uuid)) {
			return false;
		}
		PushDataQueue ret = manager.get(uuid);
		if (ret == null) {
			return false;
		}
		manager.remove(uuid);
		ret.clear();
		return true;
	}

	protected PushDataQueue get(String uuid) {
		stopCheck();
		PushDataQueue ret = manager.get(uuid);
		if (ret == null) {
			return null;
		}
		return ret;
	}

	public boolean isUUID(String uuid) {
		if (get(uuid) == null) {
			return false;
		}
		return true;
	}

	public SseElement getElement(String uuid) throws NoUUIDException {
		PushDataQueue n = get(uuid);
		if (n == null) {
			throw new NoUUIDException();
		}
		return n.element;
	}

	public void setElement(String uuid, SseElement element)
			throws NoUUIDException {
		PushDataQueue n = get(uuid);
		if (n == null) {
			throw new NoUUIDException();
		}
		n.setElement(element);
	}

	public void offer(String uuid, PushData data) throws NoUUIDException {
		PushDataQueue n = get(uuid);
		if (n == null) {
			throw new NoUUIDException();
		}
		n.exp(expLen, expTime);
		n.offer(data);
	}

	public PushData poll(String uuid) throws NoUUIDException {
		PushDataQueue n = get(uuid);
		if (n == null) {
			throw new NoUUIDException();
		}
		n.exp(expLen, expTime);
		return n.poll();
	}

	public int length(String uuid) throws NoUUIDException {
		PushDataQueue n = get(uuid);
		if (n == null) {
			throw new NoUUIDException();
		}
		n.exp(expLen, expTime);
		return n.size();
	}

	public boolean isStop() {
		return stopFlag.get() != 0;
	}

	public void load(InputStream o) throws IOException {
		byte[] b = new byte[16];
		o.read(b);
		seqManager = new Time16SequenceId(b);

		String n;
		PushDataQueue d;

		int len = SaveLoadUtil.loadInt(o);
		for (int i = 0; i < len; i++) {
			n = SaveLoadUtil.loadString(o);
			d = new PushDataQueue();
			d.load(o);
			manager.put(n, d);
		}
	}

	public void save(OutputStream o) throws IOException {
		o.write(seqManager.now());

		String n;
		PushDataQueue d;
		Iterator<String> it = manager.keySet().iterator();

		SaveLoadUtil.saveInt(o, manager.size());
		while (it.hasNext()) {
			n = it.next();
			d = manager.get(n);
			if (d == null) {
				d = new PushDataQueue();
			}
			SaveLoadUtil.saveString(o, n);
			d.save(o);
		}
	}
}
