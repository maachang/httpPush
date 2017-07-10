package com.maachang.httpPush.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Iniパラメータ管理.
 */
public class Config {

	/**
	 * パラメータ管理.
	 */
	private Map<String, Map<String, List<Object>>> params = new HashMap<String, Map<String, List<Object>>>();

	/**
	 * パラメータキー位置格納.
	 */
	private Map<String, List<Object>> keyByNum = new HashMap<String, List<Object>>();

	/**
	 * 元ファイル名.
	 */
	private String srcName = null;

	/**
	 * コンフィグファイルを読み込み.
	 * 
	 * @param conf
	 *            読み込み対象のコンフィグオブジェクトを設定します.
	 * @param name
	 *            対象のファイル名を呼び出します.
	 * @return boolean [true]の場合、読み込みは成功です.
	 * @exception Exception
	 *                例外.
	 */
	public static final boolean read(Config conf, String name) throws Exception {
		if (!Utils.isFile(name)) {
			throw new IOException("指定ファイル[" + name + "]は存在しないか、読み込めません");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					name), "UTF8"));
			ReadIni.analisys(conf, br);
			conf.srcName = Utils.getFullPath(name);
			br.close();
			br = null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		}
		return true;
	}

	/**
	 * コンフィグファイルを読み込み.
	 * 
	 * @param name
	 *            対象のファイル名を呼び出します.
	 * @return Config コンフィグオブジェクトが返却されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final Config read(String name) throws Exception {
		Config ret = new Config();
		read(ret, name);
		return ret;
	}

	/**
	 * コンストラクタ.
	 */
	public Config() {
	}

	/**
	 * 情報クリア.
	 */
	public void clear() {
		params.clear();
		keyByNum.clear();
		srcName = null;
	}

	/**
	 * 新しいパラメータを追加.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param value
	 *            対象の要素を追加します.
	 */
	public void put(String section, String key, String value) {
		if (section == null || key == null) {
			return;
		}
		if (value == null) {
			value = "";
		}
		section = section.trim();
		key = key.trim();
		List<Object> keys = keyByNum.get(section);
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue == null) {
			sectionValue = new HashMap<String, List<Object>>();
			params.put(section, sectionValue);
			keys = new ArrayList<Object>();
			keyByNum.put(section, keys);
		}
		List<Object> keyValue = sectionValue.get(key);
		if (keyValue == null) {
			keyValue = new ArrayList<Object>();
			sectionValue.put(key, keyValue);
			keys.add(key);
		}
		keyValue.add(value);
	}

	/**
	 * 新しいパラメータを設定.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param value
	 *            対象の要素を追加します.
	 */
	public void set(String section, String key, String value) {
		if (section == null || key == null) {
			return;
		}
		if (value == null) {
			value = "";
		}
		section = section.trim();
		key = key.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue == null) {
			sectionValue = new HashMap<String, List<Object>>();
			params.put(section, sectionValue);
		}
		List<Object> keyValue = sectionValue.get(key);
		if (keyValue == null) {
			keyValue = new ArrayList<Object>();
			sectionValue.put(key, keyValue);
		} else {
			keyValue.clear();
		}
		keyValue.add(value);
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return String 対象の要素が返されます.
	 */
	public String get(String section, String key, int no) {
		if (section == null || key == null || no < 0) {
			return null;
		}
		section = section.trim();
		key = key.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue != null) {
			List<Object> value = sectionValue.get(key);
			if (value != null) {
				if (value.size() > no) {
					return (String) value.get(no);
				}
			}
		}
		return null;
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return Boolean 対象の要素が返されます.
	 */
	public Boolean getBoolean(String section, String key, int no) {
		try {
			return Utils.convertBool(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return Integer 対象の要素が返されます.
	 */
	public Integer getInt(String section, String key, int no) {
		try {
			return Utils.convertInt(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return Long 対象の要素が返されます.
	 */
	public Long getLong(String section, String key, int no) {
		try {
			return Utils.convertLong(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return Double 対象の要素が返されます.
	 */
	public Double getDouble(String section, String key, int no) {
		try {
			return Utils.convertDouble(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return String 対象の要素が返されます.
	 */
	public String getString(String section, String key, int no) {
		try {
			return Utils.convertString(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return java.sql.Date 対象の要素が返されます.
	 */
	public java.sql.Date getSqlDate(String section, String key, int no) {
		try {
			return Utils.convertSqlDate(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return java.sql.Time 対象の要素が返されます.
	 */
	public java.sql.Time getSqlTime(String section, String key, int no) {
		try {
			return Utils.convertSqlTime(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return java.sql.Timestamp 対象の要素が返されます.
	 */
	public java.sql.Timestamp getSqlTimestamp(String section, String key, int no) {
		try {
			return Utils.convertSqlTimestamp(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return java.util.Date 対象の要素が返されます.
	 */
	public java.util.Date getDate(String section, String key, int no) {
		try {
			return Utils.convertSqlTimestamp(get(section, key, no));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 指定要素の内容を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @param no
	 *            対象の項番を設定します.
	 * @return String[] 対象の要素群が返されます.
	 */
	public String[] getAll(String section, String key) {
		if (section == null || key == null) {
			return null;
		}
		section = section.trim();
		key = key.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue != null) {
			List<Object> value = sectionValue.get(key);
			if (value != null && value.size() > 0) {
				int len = value.size();
				String[] ret = new String[len];
				for (int i = 0; i < len; i++) {
					ret[i] = (String) value.get(i);
				}
				return ret;
			}
		}
		return null;
	}

	/**
	 * 指定セクションを削除.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 */
	public void removeSection(String section) {
		if (section == null) {
			return;
		}
		params.remove(section);
		keyByNum.remove(section);
	}

	/**
	 * 指定キー情報を削除.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 */
	public void removeKey(String section, String key) {
		if (section == null || key == null) {
			return;
		}
		section = section.trim();
		key = key.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue != null) {
			sectionValue.remove(key);
			List<Object> keys = keyByNum.get(section);
			if (keys != null && keys.size() > 0) {
				int len = keys.size();
				for (int i = 0; i < len; i++) {
					if (key.equals(keys.get(i))) {
						keys.remove(i);
						break;
					}
				}
			}
		}
	}

	/**
	 * 指定要素数を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @return int 指定要素数が返されます.
	 */
	public int size(String section, String key) {
		if (section == null || key == null) {
			return -1;
		}
		section = section.trim();
		key = key.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue != null) {
			List<Object> value = sectionValue.get(key);
			if (value != null) {
				return value.size();
			}
		}
		return -1;
	}

	/**
	 * 指定キー数を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @return int 指定キー数が返されます.
	 */
	public int size(String section) {
		if (section == null) {
			return -1;
		}
		section = section.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue != null) {
			return sectionValue.size();
		}
		return 0;
	}

	/**
	 * 指定セクション数を取得.
	 * 
	 * @return int セクション数が返されます.
	 */
	public int size() {
		return params.size();
	}

	/**
	 * 指定キー群を取得.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @return String[] 指定キー群が返されます.
	 */
	public String[] getKeys(String section) {
		if (section == null) {
			return null;
		}
		section = section.trim();
		List<Object> keys = keyByNum.get(section);
		String[] ret = null;
		if (keys != null && keys.size() > 0) {
			int len = keys.size();
			ret = new String[len];
			for (int i = 0; i < len; i++) {
				ret[i] = (String) keys.get(i);
			}
		}
		return ret;
	}

	/**
	 * セクション名一覧を取得.
	 * 
	 * @return Object[] セクション名一覧が返されます.
	 */
	public Object[] getSections() {
		if (params != null && params.size() > 0) {
			int cnt = 0;
			String[] ret = new String[params.size()];
			Iterator<String> it = params.keySet().iterator();
			while (it.hasNext()) {
				ret[cnt++] = it.next();
			}
			Arrays.sort(ret);
			return ret;
		}
		return null;
	}

	/**
	 * 指定キー名が存在するかチェック.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @param key
	 *            対象のキー名を設定します.
	 * @return boolean [true]の場合は存在します.
	 */
	public boolean isKeys(String section, String key) {
		if (section == null) {
			return false;
		}
		section = section.trim();
		Map<String, List<Object>> sectionValue = params.get(section);
		if (sectionValue != null) {
			return (sectionValue.get(key) != null) ? true : false;
		}
		return false;
	}

	/**
	 * セクション名が存在するかチェック. <BR>
	 * 
	 * @param section
	 *            セクション名を設定します.
	 * @return boolean [true]の場合は存在します.
	 */
	public boolean isSection(String section) {
		if (section == null) {
			return false;
		}
		return params.containsKey(section.trim());
	}

	/**
	 * 指定セクション以下のコンフィグ情報を生成.
	 * 
	 * @param section
	 *            対象のセクション名を設定します.
	 * @return Config 複製されたコンフィグ情報が返されます. [null]の場合、指定セクション情報は存在しません.
	 */
	public Config getSectionByConfig(String section) {
		if (section == null) {
			return null;
		}
		section = section.trim();
		if (!params.containsKey(section)) {
			return null;
		}
		Map<String, List<Object>> n = params.get(section);
		Config ret = new Config();
		Iterator<String> it = n.keySet().iterator();
		String key;
		int len, i;
		List<Object> lst;
		while (it.hasNext()) {
			key = it.next();
			lst = n.get(key);
			len = lst.size();
			for (i = 0; i < len; i++) {
				ret.put(section, key, (String) lst.get(i));
			}
		}
		return ret;
	}

	/**
	 * 対象ファイル名を取得.
	 * 
	 * @return String 対象のファイル名が返却されます.
	 */
	public String getSrcName() {
		return srcName;
	}
}
