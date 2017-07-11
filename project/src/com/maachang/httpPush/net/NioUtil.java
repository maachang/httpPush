package com.maachang.httpPush.net;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public final class NioUtil {
	private NioUtil() {
	}

	/**
	 * SelectionKeyの破棄.
	 * 
	 * @param key
	 *            破棄対象のSelectionKeyを設定します.
	 */
	public static final void destroyKey(SelectionKey key) {
		if (key != null) {
			try {
				NioElement em = (NioElement) key.attachment();
				if (em != null) {
					em.clear();
					return;
				}
			} catch (Exception e) {
			}
			try {
				key.cancel();
			} catch (Exception e) {
			}
			if (key.channel() instanceof SocketChannel) {
				try {
					((SocketChannel) key.channel()).socket().close();
				} catch (Throwable e) {
				}
				try {
					key.channel().close();
				} catch (Throwable e) {
				}
			} else {
				try {
					key.channel().close();
				} catch (Throwable e) {
				}
			}
		}
	}

	/**
	 * サーバーソケット作成.
	 * 
	 * @param recvBuffer
	 * @param addr
	 * @param port
	 * @param backlog
	 * @exception Exception
	 *                例外.
	 */
	public static final ServerSocketChannel createServerSocketChannel(
			int recvBuffer, String addr, int port, int backlog)
			throws Exception {
		// nio:サーバーソケット作成.
		ServerSocketChannel ch = ServerSocketChannel.open();
		ch.configureBlocking(false);
		ch.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		ch.setOption(StandardSocketOptions.SO_RCVBUF, recvBuffer);

		// サーバーソケットBind.
		if (addr == null || (addr = addr.trim()).length() == 0) {
			ch.socket().bind(new InetSocketAddress(port), backlog);
		} else {
			ch.socket().bind(new InetSocketAddress(addr, port), backlog);
		}
		return ch;
	}

	/**
	 * SocketChannelを初期化.
	 * 
	 * @param channel
	 *            SocketChannelを設定します.
	 * @param s
	 *            送信バッファを設定します.
	 * @param r
	 *            受信バッファを設定します.
	 * @param k
	 *            keepAliveモードを設定します.
	 * @param t
	 *            tcpNoDeleyモードを設定します.
	 * @return boolean [true]の場合初期化成功です.
	 * @exception Exception
	 *                例外.
	 */
	public static final boolean initSocket(SocketChannel channel, int s, int r,
			boolean k, boolean t) throws Exception {
		try {
			channel.configureBlocking(false);
			channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, k);
			channel.setOption(StandardSocketOptions.TCP_NODELAY, t);
			channel.setOption(StandardSocketOptions.SO_SNDBUF, s);
			channel.setOption(StandardSocketOptions.SO_RCVBUF, r);
		} catch (Exception e) {
			try {
				channel.close();
			} catch (Throwable ee) {
				return false;
			}
		}
		return true;
	}
}
