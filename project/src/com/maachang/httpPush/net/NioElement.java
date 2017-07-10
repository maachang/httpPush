package com.maachang.httpPush.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.maachang.httpPush.util.ByteArrayIO;
import com.maachang.httpPush.util.atomic.AtomicNumber32;

/**
 * 基本Nio要素.
 */
public abstract class NioElement {
	protected volatile boolean connectionFlag = false;
	protected NioSelector selector;
	protected SelectionKey key;
	protected final AtomicNumber32 ops = new AtomicNumber32(
			SelectionKey.OP_READ);
	protected ByteArrayIO buffer = new ByteArrayIO(512);

	protected SendData sendData = new SendData(this);
	protected SendLess sendLess = new SendLess();

	public NioElement() {
	}

	/**
	 * オブジェクトクリア.
	 */
	public void clear() {
		connectionFlag = false;
		selector = null;
		if (sendLess != null) {
			sendLess.clear();
			sendLess = null;
		}
		if (sendData != null) {
			sendData.clear();
			sendData = null;
		}
		if (buffer != null) {
			buffer.clear();
			buffer = null;
		}
		if (key != null) {
			key.attach(null);
			NioUtil.destroyKey(key);
			key = null;
		}
	}

	/**
	 * 要素が有効かチェック.
	 * 
	 * @return boolean [true]の場合、接続中です.
	 */
	public boolean isConnection() {
		return connectionFlag;
	}

	/**
	 * 対象要素と、対象Socket情報を、セレクタに登録.
	 * 
	 * @param selector
	 *            登録先のセレクタを設定します.
	 * @param channel
	 *            対象のソケットチャネルを設定します.
	 * @param op
	 *            対象の処理モードを設定します.
	 * @return SelectionKey 生成されたSelectionKeyを返却します.
	 * @exception Exception
	 *                IO例外.
	 */
	public SelectionKey registor(NioSelector selector, SocketChannel channel,
			int op) throws Exception {
		SelectionKey ret = selector.register(channel, op, this);
		this.key = ret;
		this.selector = selector;
		this.connectionFlag = true;
		return ret;
	}

	/**
	 * SelectedKeyを取得.
	 * 
	 * @return SelectionKey SelectionKeyが返却されます.
	 */
	public SelectionKey getKey() {
		return key;
	}

	/**
	 * Selectorを取得.
	 * 
	 * @return NioSelector Selectorが返却されます.
	 */
	public NioSelector getSelector() {
		return selector;
	}

	/**
	 * 受信バッファを取得.
	 * 
	 * @return ByteArrayIO 受信バッファが返却されます.
	 */
	public ByteArrayIO getBuffer() {
		return buffer;
	}

	/**
	 * SendLessオブジェクトを取得.
	 * 
	 * @return SendLess オブジェクトが返却されます.
	 */
	public SendLess getSendLess() {
		return sendLess;
	}

	/**
	 * SendDataオブジェクトを取得.
	 * 
	 * @return SendData オブジェクトが返却されます.
	 */
	public SendData getSendData() {
		return sendData;
	}

	/**
	 * interOpsの変更.
	 * 
	 * @param ops
	 *            対象のOpsを設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public void interestOps(int ops) throws IOException {
		this.ops.set(ops);
		key.interestOps(ops);
		selector.wakeup();
	}

	/**
	 * 現在のinterOpsを取得.
	 * 
	 * @return int 対象のOpsが返却されます.
	 */
	public int interestOps() {
		return ops.get();
	}
}
