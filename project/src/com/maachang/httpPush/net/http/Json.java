package com.maachang.httpPush.net.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Json変換処理.
 */
@SuppressWarnings("unchecked")
public final class Json {
	private Json() {
	}

	private static final int TYPE_ARRAY = 0;
	private static final int TYPE_MAP = 1;

	/**
	 * JSON変換.
	 * 
	 * @param target
	 *            対象のターゲットオブジェクトを設定します.
	 * @return String 変換されたJSON情報が返されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final String encode(Object target) throws Exception {
		StringBuilder buf = new StringBuilder();
		_encode(buf, target, target);
		return buf.toString();
	}

	/**
	 * JSON形式から、オブジェクト変換2.
	 * 
	 * @param json
	 *            対象のJSON情報を設定します.
	 * @return Object 変換されたJSON情報が返されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final Object decode(String json) throws IOException {
		if (json == null) {
			return null;
		}

		List<Object> list;
		int[] n = new int[1];
		while (true) {
			// token解析が必要な場合.
			if (json.startsWith("[") || json.startsWith("{")) {
				// JSON形式をToken化.
				list = analysisJsonToken(json);
				// Token解析処理.
				if ("[".equals(list.get(0))) {
					// List解析.
					return createJsonInfo(n, list, TYPE_ARRAY, 0, list.size());
				} else {
					// Map解析.
					return createJsonInfo(n, list, TYPE_MAP, 0, list.size());
				}
			} else if (json.startsWith("(") && json.endsWith(")")) {
				json = json.substring(1, json.length() - 1).trim();
				continue;
			}
			break;
		}
		return decJsonValue(n, 0, json);
	}

	/** [encodeJSON]jsonコンバート. **/
	private static final void _encode(StringBuilder buf, Object base,
			Object target) throws IOException {
		if (target instanceof Map) {
			encodeJsonMap(buf, base, (Map) target);
		} else if (target instanceof List) {
			encodeJsonList(buf, base, (List) target);
		} else if (target instanceof Long || target instanceof Short
				|| target instanceof Integer || target instanceof Float
				|| target instanceof Double || target instanceof BigInteger
				|| target instanceof BigDecimal) {
			buf.append(target);
		} else if (target instanceof Character || target instanceof String) {
			buf.append("\"").append(target).append("\"");
		} else if (target instanceof byte[]) {
			buf.append("null");
		} else if (target instanceof char[]) {
			buf.append("\"").append(new String((char[]) target)).append("\"");
		} else if (target instanceof java.util.Date) {
			buf.append("\"").append(dateToString((java.util.Date) target))
					.append("\"");
		} else if (target instanceof Boolean) {
			buf.append(target);
		} else if (target == null) {
			buf.append("null");
		} else if (target.getClass().isArray()) {
			if (Array.getLength(target) == 0) {
				buf.append("[]");
			} else {
				encodeJsonArray(buf, base, target);
			}
		} else {
			buf.append("null");
		}
	}

	/** [encodeJSON]jsonMapコンバート. **/
	private static final void encodeJsonMap(StringBuilder buf, Object base,
			Map map) throws IOException {
		boolean flg = false;
		Map mp = (Map) map;
		Iterator it = mp.keySet().iterator();
		buf.append("{");
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = mp.get(key);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			buf.append("\"").append(key).append("\":");
			_encode(buf, base, value);
		}
		buf.append("}");
	}

	/** [encodeJSON]jsonListコンバート. **/
	private static final void encodeJsonList(StringBuilder buf, Object base,
			List list) throws IOException {
		boolean flg = false;
		List lst = (List) list;
		buf.append("[");
		int len = lst.size();
		for (int i = 0; i < len; i++) {
			Object value = lst.get(i);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			_encode(buf, base, value);
		}
		buf.append("]");
	}

	/** [encodeJSON]json配列コンバート. **/
	private static final void encodeJsonArray(StringBuilder buf, Object base,
			Object list) throws IOException {
		boolean flg = false;
		int len = Array.getLength(list);
		buf.append("[");
		for (int i = 0; i < len; i++) {
			Object value = Array.get(list, i);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			_encode(buf, base, value);
		}
		buf.append("]");
	}

	/** [decodeJSON]１つの要素を変換. **/
	private static final Object decJsonValue(int[] n, int no, String json)
			throws IOException {
		int len;
		if ((len = json.length()) <= 0) {
			return json;
		}
		// 文字列コーテーション区切り.
		if ((json.startsWith("\"") && json.endsWith("\""))
				|| (json.startsWith("\'") && json.endsWith("\'"))) {
			json = json.substring(1, len - 1);

			// ISO8601の日付フォーマットかチェック.
			if (isISO8601(json)) {
				return stringToDate(json);
			}
			return json;
		}
		// NULL文字.
		else if ("null".equals(json)) {
			return null;
		}
		// BOOLEAN true.
		else if ("true".equals(json)) {
			return Boolean.TRUE;
		}
		// BOOLEAN false.
		else if ("false".equals(json)) {
			return Boolean.FALSE;
		}
		// 数値.
		if (isNumeric(json)) {
			if (json.indexOf(".") != -1) {
				return Double.parseDouble(json);
			}
			return Long.parseLong(json);
		}
		// その他.
		throw new IOException("JSON解析に失敗(" + json + "):No:" + no);
	}

	/** JSON_Token_解析処理 **/
	private static final List<Object> analysisJsonToken(String json)
			throws IOException {
		int s = -1;
		char c;
		int cote = -1;
		int bef = -1;
		int len = json.length();
		List<Object> ret = new ArrayList<Object>();
		// Token解析.
		for (int i = 0; i < len; i++) {
			c = json.charAt(i);
			// コーテーション内.
			if (cote != -1) {
				// コーテーションの終端.
				if (bef != '\\' && cote == c) {
					ret.add(json.substring(s - 1, i + 1));
					cote = -1;
					s = i + 1;
				}
			}
			// コーテーション開始.
			else if (bef != '\\' && (c == '\'' || c == '\"')) {
				cote = c;
				if (s != -1 && s != i && bef != ' ' && bef != '　'
						&& bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i + 1));
				}
				s = i + 1;
				bef = -1;
			}
			// ワード区切り.
			else if (c == '[' || c == ']' || c == '{' || c == '}' || c == '('
					|| c == ')' || c == ':' || c == ';' || c == ','
					|| (c == '.' && (bef < '0' || bef > '9'))) {
				if (s != -1 && s != i && bef != ' ' && bef != '　'
						&& bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				ret.add(new String(new char[] { c }));
				s = i + 1;
			}
			// 連続空間区切り.
			else if (c == ' ' || c == '　' || c == '\t' || c == '\n'
					|| c == '\r') {
				if (s != -1 && s != i && bef != ' ' && bef != '　'
						&& bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				s = -1;
			}
			// その他文字列.
			else if (s == -1) {
				s = i;
			}
			bef = c;
		}
		return ret;
	}

	/** Json-Token解析. **/
	private static final Object createJsonInfo(int[] n, List<Object> token,
			int type, int no, int len) throws IOException {
		String value;
		StringBuilder before = null;
		// List.
		if (type == TYPE_ARRAY) {
			List<Object> ret = new ArrayList<Object>();
			int flg = 0;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (",".equals(value) || "]".equals(value)) {
					if ("]".equals(value)) {
						if (flg == 1) {
							if (before != null) {
								ret.add(decJsonValue(n, i, before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (flg == 1) {
							if (before == null) {
								ret.add(null);
							} else {
								ret.add(decJsonValue(n, i, before.toString()));
							}
						}
					}
					before = null;
					flg = 0;
				} else if ("[".equals(value)) {
					ret.add(createJsonInfo(n, token, 0, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else if ("{".equals(value)) {
					ret.add(createJsonInfo(n, token, 1, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
					flg = 1;
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// map.
		else if (type == TYPE_MAP) {
			Map<String, Object> ret;
			ret = new HashMap<String, Object>();
			String key = null;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (":".equals(value)) {
					if (key == null) {
						throw new IOException("Map形式が不正です(No:" + i + ")");
					}
				} else if (",".equals(value) || "}".equals(value)) {
					if ("}".equals(value)) {
						if (key != null) {
							if (before == null) {
								ret.put(key, null);
							} else {
								ret.put(key, decJsonValue(n, i, before
										.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (key == null) {
							if (before == null) {
								continue;
							}
							throw new IOException("Map形式が不正です(No:" + i + ")");
						}
						if (before == null) {
							ret.put(key, null);
						} else {
							ret.put(key, decJsonValue(n, i, before.toString()));
						}
						before = null;
						key = null;
					}
				} else if ("[".equals(value)) {
					if (key == null) {
						throw new IOException("Map形式が不正です(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, 0, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if ("{".equals(value)) {
					if (key == null) {
						throw new IOException("Map形式が不正です(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, 1, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if (key == null) {
					key = value;
					if ((key.startsWith("'") && key.endsWith("'"))
							|| (key.startsWith("\"") && key.endsWith("\""))) {
						key = key.substring(1, key.length() - 1).trim();
					}
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// その他.
		throw new IOException("JSON解析に失敗");
	}

	/** 日付情報チェック. **/
	private static final boolean isNumeric(String o) {
		try {
			Double.parseDouble(o);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/** 日付を文字変換. **/
	private static final String dateToString(Date d) {
		return _getISO8601().format(d);
	}

	/** 文字を日付変換. **/
	private static final Date stringToDate(String s) throws IOException {
		try {
			return _getISO8601().parse(s);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/** 指定文字が日付フォーマットの可能性かチェック. **/
	private static final boolean isISO8601(String s) {
		int code = 0;
		int len = s.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);
			switch (code) {
			case 0:
			case 1:
				if (c == '-') {
					code++;
				} else if (!(c >= '0' && c <= '9')) {
					return false;
				}
				break;
			case 2:
				if (c == 'T') {
					code++;
				} else if (!(c >= '0' && c <= '9')) {
					return false;
				}
				break;
			case 3:
			case 4:
				if (c == ':') {
					code++;
				} else if (!(c >= '0' && c <= '9')) {
					return false;
				}
				break;
			case 5:
				return true;
			}
		}
		return false;
	}

	// 日付フォーマットを管理.
	private static final ThreadLocal<WeakReference<SimpleDateFormat>> iso8601 = new ThreadLocal<WeakReference<SimpleDateFormat>>();

	private static final SimpleDateFormat _getISO8601() {
		WeakReference<SimpleDateFormat> n = iso8601.get();
		if (n == null || n.get() == null) {
			n = new WeakReference<SimpleDateFormat>(new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssXXX"));
			iso8601.set(n);
		}
		return n.get();
	}
}
