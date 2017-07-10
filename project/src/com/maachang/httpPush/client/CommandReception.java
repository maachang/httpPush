package com.maachang.httpPush.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.util.Utils;

/**
 * コマンドから、Reception接続.
 */
public class CommandReception {
	private static final CommandReception cmd = new CommandReception();
	private ReceptionClient client = null;

	public static final void main(String[] args) throws Exception {
		cmd.execute(args);
	}

	private final void execute(String[] args) throws Exception {
		String in;
		List<String> cmd;
		BufferedReader buf = new BufferedReader(
				new InputStreamReader(System.in));
		System.out.println("httpPush " + Def.VERSION + " receptoion");
		while (true) {
			try {
				System.out.print(">");
				in = buf.readLine();
				if (in == null || (in = in.trim()).length() == 0) {
					continue;
				}
				if ("quit".equals(in) || "exit".equals(in)) {
					return;
				} else if ("help".equals(in)) {
					viewHelp();
					continue;
				}
				cmd = Utils.cutString(true, false, in, " ");
				executeCommand(cmd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private final void viewHelp() {
		System.out.println("※ (b) boolean (s) string (n)integer.");
		System.out.println("open domain(s)");
		System.out.println("open domain(s) ssl(b) port(n) timeout(n)");
		System.out.println("close");
		System.out.println("connect");
		System.out.println("reconnect uuid(s)");
		System.out.println("disconnect");
		System.out.println("size");
		System.out.println("send data(s)");
		System.out.println("send uuid(s) data(s)");
		System.out.println("uuid");
		System.out.println("isconnect");
	}

	private final void executeCommand(List<String> cmd) throws Exception {
		String method = cmd.get(0).toLowerCase();

		if ("open".equals(method)) {
			cmdOpen(cmd);
		} else if ("close".equals(method)) {
			cmdClose();
		} else if ("connect".equals(method)) {
			cmdConnect();
		} else if ("reconnect".equals(method)) {
			cmdReconnect(cmd);
		} else if ("disconnect".equals(method)) {
			cmdDisconnect();
		} else if ("size".equals(method)) {
			cmdSize();
		} else if ("send".equals(method)) {
			cmdSend(cmd);
		} else if ("uuid".equals(method)) {
			cmdUuid();
		} else if ("isconnect".equals(method)) {
			cmdIsConnect();
		} else {
			System.out.println("対象のコマンドはありません");
		}
	}

	private final boolean noInitClient() {
		if (client == null) {
			System.out.println("接続定義[open]が行われていません");
			return true;
		}
		return false;
	}

	private final void cmdOpen(List<String> cmd) throws Exception {
		if (client != null) {
			client = null;
		}
		switch (cmd.size()) {
		case 1:
			System.out.println("パラメータが足りません");
			return;
		case 2:
			client = new ReceptionClient(cmd.get(1));
			System.out.println("success");
			return;
		case 3:
			client = new ReceptionClient(Utils.parseBoolean(cmd.get(2)), cmd
					.get(1));
			System.out.println("success");
			return;
		case 4:
			client = new ReceptionClient(Utils.parseBoolean(cmd.get(2)), cmd
					.get(1), Utils.parseInt(cmd.get(3)));
			System.out.println("success");
			return;
		default:
			client = new ReceptionClient(Utils.parseBoolean(cmd.get(2)), cmd
					.get(1), Utils.parseInt(cmd.get(3)), Utils.parseInt(cmd
					.get(4)));
			System.out.println("success");
			return;
		}
	}

	private final void cmdClose() throws Exception {
		if (noInitClient()) {
			return;
		}
		client = null;
		System.out.println("success");
	}

	private final void cmdConnect() throws Exception {
		if (noInitClient()) {
			return;
		}
		client.connect(new ReceptionCallback() {
			public void onResult(Map<String, Object> result) {
				if (client.uuid.length() != 0) {
					System.out.println("既に接続中です");
				} else {
					System.out.println("uuid:" + result.get("uuid"));
				}
			}
		});
	}

	private final void cmdReconnect(List<String> cmd) throws Exception {
		if (noInitClient()) {
			return;
		}
		switch (cmd.size()) {
		case 1:
			System.out.println("パラメータが足りません");
			return;
		default:
			if (client.reconnect(cmd.get(1))) {
				System.out.println("success");
			} else {
				System.out.println("error 指定情報はuuidではありません");
			}
			return;
		}
	}

	private final void cmdDisconnect() throws Exception {
		if (noInitClient()) {
			return;
		}
		client.disconnect(new ReceptionCallback() {
			public void onResult(Map<String, Object> result) {
				client.uuid = "";
				System.out.println("disconnect = " + result.get("result"));
			}
		});
	}

	private final void cmdSize() throws Exception {
		if (noInitClient()) {
			return;
		}
		client.size(new ReceptionCallback() {
			public void onResult(Map<String, Object> result) {
				System.out.println("size = " + result.get("size"));
			}
		});
	}

	private final void cmdSend(List<String> cmd) throws Exception {
		if (noInitClient()) {
			return;
		}
		switch (cmd.size()) {
		case 1:
			System.out.println("パラメータが足りません");
			return;
		case 2:
			client.send(cmd.get(1), new ReceptionCallback() {
				public void onResult(Map<String, Object> result) {
					System.out.println("send = " + result.get("result"));
				}
			});
			return;
		default:
			client.connectSend(cmd.get(1), cmd.get(2), new ReceptionCallback() {
				public void onResult(Map<String, Object> result) {
					System.out.println("send = " + result.get("result"));
				}
			});
			return;
		}
	}

	private final void cmdUuid() throws Exception {
		if (noInitClient()) {
			return;
		}
		System.out.println("uuid = " + client.getUUID());
	}

	private final void cmdIsConnect() throws Exception {
		System.out.println("connect = "
				+ (client != null && Utils.useString(client.getUUID())));
	}
}
