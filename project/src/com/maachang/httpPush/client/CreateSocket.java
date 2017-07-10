package com.maachang.httpPush.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.maachang.httpPush.util.atomic.AtomicNumber32;

/**
 * Socket生成.
 */
final class CreateSocket {
	protected CreateSocket() {
	}

	/** TRUST_PASS. **/
	private static final char[] TRUST_PASS = "changeit".toCharArray();
	private static final Object sync = new Object();
	private static final AtomicNumber32 sslFactoryFlag = new AtomicNumber32(0);
	private static SocketFactory sslFactory = null;

	/** SSLSocketFactory作成. **/
	protected static final SocketFactory getSSLSocketFactory()
			throws IOException {
		if (sslFactoryFlag.get() == 0) {
			synchronized (sync) {
				InputStream in = null;
				try {
					in = CreateSocket.class.getResourceAsStream("cacerts");
					KeyStore t = KeyStore.getInstance("JKS");
					t.load(in, TRUST_PASS);
					in.close();
					in = null;

					TrustManagerFactory tmf = TrustManagerFactory
							.getInstance("SunX509");
					tmf.init(t);
					t = null;

					// ソケットを生成する
					SSLContext context = SSLContext.getInstance("TLS");
					context.init(null, tmf.getTrustManagers(), null);
					SSLSocketFactory s = context.getSocketFactory();
					sslFactory = s;
					sslFactoryFlag.set(1);
				} catch (Exception e) {
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e) {
						}
					}
				}
			}
		}
		return sslFactory;
	}

	/** Socket基本オプション. **/
	private static final int LINGER = 0;
	private static final int SENDBUF = 1024;
	private static final int RECVBUF = 2048;
	private static final boolean TCP_NODELAY = true;
	private static final boolean KEEP_ALIVE = false;

	/** Httpソケットオプションをセット. **/
	private static final void setSocketOption(Socket soc, int timeout)
			throws IOException {
		soc.setReuseAddress(true);
		soc.setSoLinger(true, LINGER);
		soc.setSendBufferSize(SENDBUF);
		soc.setReceiveBufferSize(RECVBUF);
		soc.setKeepAlive(KEEP_ALIVE);
		soc.setTcpNoDelay(TCP_NODELAY);
		soc.setSoTimeout(timeout);
	}

	/**
	 * Socket作成.
	 * 
	 * @param ssl
	 *            [true]の場合、SSLで接続します.
	 * @param addr
	 *            接続先アドレス(domain)を設定します.
	 * @param port
	 *            対象のポート番号を設定します.
	 * @param timeout
	 *            通信タイムアウト値を設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public static final Socket create(boolean ssl, String addr, int port,
			int timeout) throws IOException {
		if (ssl) {
			return createSSL(addr, port, timeout);
		} else {
			return createSocket(addr, port, timeout);
		}
	}

	/** SSLSocket生成. **/
	private static final Socket createSSL(String addr, int port, int timeout)
			throws IOException {
		SSLSocket ret = null;
		try {
			SSLSocketFactory factory = (SSLSocketFactory) getSSLSocketFactory();
			ret = (SSLSocket) factory.createSocket();
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), timeout);
			ret.startHandshake();
		} catch (IOException e) {
			if (ret != null) {
				try {
					ret.close();
				} catch (Exception ee) {
				}
			}
			throw e;
		}
		return ret;
	}

	/** Socket生成. **/
	private static final Socket createSocket(String addr, int port, int timeout)
			throws IOException {
		Socket ret = new Socket();
		try {
			setSocketOption(ret, timeout);
			ret.connect(new InetSocketAddress(addr, port), timeout);
		} catch (IOException e) {
			try {
				ret.close();
			} catch (Exception ee) {
			}
			throw e;
		}
		return ret;
	}
}
