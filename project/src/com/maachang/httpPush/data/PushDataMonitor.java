package com.maachang.httpPush.data;

import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.maachang.httpPush.sse.SseElement;

/**
 * PushData監視.
 */
final class PushDataMonitor extends Thread {
	private volatile boolean stopFlag = true;
	private PushDataManager manager = null;

	/** LOG. **/
	private static final Log LOG = LogFactory.getLog(PushDataMonitor.class);

	public PushDataMonitor(PushDataManager manager) {
		this.manager = manager;
	}

	public void startThread() {
		stopFlag = false;
		setDaemon(true);
		start();
	}

	public void stopThread() {
		stopFlag = true;
	}

	public boolean isStopThread() {
		return stopFlag;
	}

	public void run() {
		startMonitor();
	}

	public final void startMonitor() {

		LOG.info("#### start PushDataMonitor");

		Map<String, PushDataQueue> map = manager.manager;
		int expLen = manager.expLen;
		long expTime = manager.expTime;
		long ctime = manager.closeTimeout;

		boolean endFlag = false;
		Iterator<String> it;
		String key;
		PushDataQueue queue;
		SseElement sse;

		ThreadDeath ret = null;
		while (!endFlag && !stopFlag) {
			try {
				while (!endFlag && !stopFlag) {

					// 停止フラグが立っている場合は、処理しない.
					// データがゼロ件の場合も処理しない.
					if (manager.stopFlag.get() != 0 || map.size() == 0) {
						Thread.sleep(100);
						continue;
					}

					it = map.keySet().iterator();
					while (it.hasNext()) {

						// 停止フラグが立っている場合は、処理しない.
						if (manager.stopFlag.get() != 0) {
							break;
						}
						try {

							// 1件データを取得.
							key = it.next();
							queue = map.get(key);
							sse = queue.element;

							// expire情報をクリア.
							queue.exp(expLen, expTime);

							// SSE側が切断されている場合.
							if (sse == null || !sse.isConnection()) {

								// 最終アクセス時間から一定時間を経過した場合は、
								// 対象要素をクリア.
								if (System.currentTimeMillis() > queue.lastAccessTime
										.get()
										+ ctime) {
									queue.clear();
									it.remove();
									continue;
								}

								// SSE側が存在し接続中の場合
								// だたし、書き込み処理中で無い場合.
							} else if ((sse.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {

								// 送信対象のデータが存在する場合.
								if (queue.size() > 0) {

									// 書き込みをON.
									sse.sendMode();
								}
							}

						} catch (Exception e) {
							if (e instanceof InterruptedException) {
								throw e;
							}
							LOG.error("error", e);
						} finally {

							// 一定停止.
							Thread.sleep(5);
						}
					}
				}
			} catch (Throwable to) {
				if (to instanceof InterruptedException) {
					endFlag = true;
				} else if (to instanceof ThreadDeath) {
					endFlag = true;
					ret = (ThreadDeath) to;
				}
			}

		}

		LOG.info("#### stop PushDataMonitor");

		if (ret != null) {
			throw ret;
		}
	}
}
