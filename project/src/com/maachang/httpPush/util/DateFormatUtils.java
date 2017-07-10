package com.maachang.httpPush.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 日付フォーマット処理系ユーティリティ. 3種類の日付フォーマット変換に対応. 1)20130301 -> java.sql.Date(
 * 2013-1900,3-1,1 ) 連続した数値文字列のフォーマットを解析.
 * 
 * 2)2013/03/01 -> java.sql.Date( 2013-1900,3-1,1 ) 13/03/01 -> java.sql.Date(
 * 2013-1900,3-1,1 ) 数値以外の区切り文字条件を解析.
 * 
 * 3)Wed, 19-Mar-2014 03:55:33 GMT Web日付フォーマットの内容をjava.sql.Timestampに変換します.
 */
@SuppressWarnings("deprecation")
public final class DateFormatUtils {
	private DateFormatUtils() {
	}

	/** グリニッジ標準時タイムゾーン. **/
	protected static final TimeZone GMT_TIMEZONE = TimeZone
			.getTimeZone("Europe/London");

	/** 先頭日付の文字数に併せた、西暦取得. **/
	private static final int getYear(String v) {
		String h = String.valueOf((new java.util.Date().getYear() + 1900));
		switch (v.length()) {
		case 0:
			return Utils.parseInt(h) - 1900;
		case 1:
			return Utils.parseInt(h.substring(0, h.length() - 1) + v) - 1900;
		case 2:
			return Utils.parseInt(h.substring(0, h.length() - 2) + v) - 1900;
		case 3:
			return Utils.parseInt(h.substring(0, h.length() - 3) + v) - 1900;
		case 4:
			return Utils.parseInt(v) - 1900;
		}
		throw new ConvertException("日付の桁数が[" + v.length() + "]と不正です");
	}

	/** ミリ秒に対するナノ秒変換. **/
	private static final int getMilliByNano(String v) {
		if (v.length() > 3) {
			v = v.substring(0, 3);
		}
		return Utils.parseInt(v) * 1000000;
	}

	/** 区切り文字による日付フォーマット変換. **/
	private static final java.util.Date cutDate(int type, String value) {
		char c;
		int len = value.length();
		List<String> list = new ArrayList<String>();
		StringBuilder buf = null;
		for (int i = 0; i < len; i++) {
			c = value.charAt(i);
			if (c >= '0' && c <= '9') {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(c);
			} else if (buf != null) {
				list.add(buf.toString());
				buf = null;
			}
		}
		if (buf != null) {
			list.add(buf.toString());
			buf = null;
		}

		// java.sql.Date.
		len = list.size();
		if (type == 0) {
			switch (len) {
			case 0:
				Date d = new java.util.Date();
				return new java.sql.Date(d.getYear(), d.getMonth(), d.getDate());
			case 1:
				return new java.sql.Date(getYear(list.get(0)), 0, 1);
			case 2:
				return new java.sql.Date(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1, 1);
			default:
				return new java.sql.Date(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1, Utils.parseInt(list.get(2)));
			}
		}
		// java.sql.Time.
		else if (type == 1) {
			switch (len) {
			case 0:
				Date d = new java.util.Date();
				return new java.sql.Time(d.getHours(), d.getMinutes(), d
						.getSeconds());
			case 1:
				return new java.sql.Time(Utils.parseInt(list.get(0)), 0, 0);
			case 2:
				return new java.sql.Time(Utils.parseInt(list.get(0)), Utils
						.parseInt(list.get(1)), 0);
			default:
				return new java.sql.Time(Utils.parseInt(list.get(0)), Utils
						.parseInt(list.get(1)), Utils.parseInt(list.get(2)));
			}
		}
		// java.sql.Timestamp.
		else {
			switch (len) {
			case 0:
				return new java.sql.Timestamp(System.currentTimeMillis());
			case 1:
				return new java.sql.Timestamp(getYear(list.get(0)), 0, 1, 0, 0,
						0, 0);
			case 2:
				return new java.sql.Timestamp(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1, 1, 0, 0, 0, 0);
			case 3:
				return new java.sql.Timestamp(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1,
						Utils.parseInt(list.get(2)), 0, 0, 0, 0);
			case 4:
				return new java.sql.Timestamp(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1,
						Utils.parseInt(list.get(2)), Utils
								.parseInt(list.get(3)), 0, 0, 0);
			case 5:
				return new java.sql.Timestamp(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1,
						Utils.parseInt(list.get(2)), Utils
								.parseInt(list.get(3)), Utils.parseInt(list
								.get(4)), 0, 0);
			case 6:
				return new java.sql.Timestamp(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1,
						Utils.parseInt(list.get(2)), Utils
								.parseInt(list.get(3)), Utils.parseInt(list
								.get(4)), Utils.parseInt(list.get(5)), 0);
			default:
				return new java.sql.Timestamp(getYear(list.get(0)), Utils
						.parseInt(list.get(1)) - 1,
						Utils.parseInt(list.get(2)), Utils
								.parseInt(list.get(3)), Utils.parseInt(list
								.get(4)), Utils.parseInt(list.get(5)),
						getMilliByNano(list.get(6)));
			}
		}
	}

	/** 連続文字での日付フォーマット変換. **/
	private static final java.util.Date stringDate(int type, String value) {
		char c;
		int len = value.length();
		for (int i = 0; i < len; i++) {
			c = value.charAt(i);
			if (!(c >= '0' && c <= '9')) {
				// 数値以外の条件が格納されている場合は処理しない.
				return null;
			}
		}

		// java.sql.Date.
		if (type == 0) {
			if (len < 4) {
				if (len == 0) {
					Date d = new java.util.Date();
					return new java.sql.Date(d.getYear(), d.getMonth(), d
							.getDate());
				} else {
					return new java.sql.Date(getYear(value), 0, 1);
				}
			}
			if (len < 6) {
				return new java.sql.Date(getYear(value), 0, 1);
			} else if (len < 8) {
				return new java.sql.Date(getYear(value.substring(0, 4)), Utils
						.parseInt(value.substring(4, 6)) - 1, 1);
			} else {
				return new java.sql.Date(getYear(value.substring(0, 4)), Utils
						.parseInt(value.substring(4, 6)) - 1, Utils
						.parseInt(value.substring(6, 8)));
			}
		}
		// java.sql.Time.
		else if (type == 1) {
			if (len < 2) {
				if (len == 0) {
					Date d = new java.util.Date();
					return new java.sql.Time(d.getHours(), d.getMinutes(), d
							.getSeconds());
				} else {
					return new java.sql.Time(Utils.parseInt(value), 0, 0);
				}
			}
			if (len < 4) {
				return new java.sql.Time(Utils.parseInt(value.substring(0, 2)),
						0, 0);
			} else if (len < 6) {
				return new java.sql.Time(Utils.parseInt(value.substring(0, 2)),
						Utils.parseInt(value.substring(2, 4)), 0);
			} else {
				return new java.sql.Time(Utils.parseInt(value.substring(0, 2)),
						Utils.parseInt(value.substring(2, 4)), Utils
								.parseInt(value.substring(4, 6)));
			}
		}
		// java.sql.Timestamp.
		else {
			if (len < 4) {
				if (len == 0) {
					return new java.sql.Timestamp(System.currentTimeMillis());
				} else {
					return new java.sql.Timestamp(getYear(value), 0, 1, 0, 0,
							0, 0);
				}
			}
			if (len < 6) {
				return new java.sql.Timestamp(getYear(value), 0, 1, 0, 0, 0, 0);
			} else if (len < 8) {
				return new java.sql.Timestamp(getYear(value.substring(0, 4)),
						Utils.parseInt(value.substring(4, 6)) - 1, 1, 0, 0, 0,
						0);
			} else if (len < 10) {
				return new java.sql.Timestamp(getYear(value.substring(0, 4)),
						Utils.parseInt(value.substring(4, 6)) - 1, Utils
								.parseInt(value.substring(6, 8)), 0, 0, 0, 0);
			} else if (len < 12) {
				return new java.sql.Timestamp(getYear(value.substring(0, 4)),
						Utils.parseInt(value.substring(4, 6)) - 1, Utils
								.parseInt(value.substring(6, 8)), Utils
								.parseInt(value.substring(8, 10)), 0, 0, 0);
			} else if (len < 14) {
				return new java.sql.Timestamp(getYear(value.substring(0, 4)),
						Utils.parseInt(value.substring(4, 6)) - 1, Utils
								.parseInt(value.substring(6, 8)), Utils
								.parseInt(value.substring(8, 10)), Utils
								.parseInt(value.substring(10, 12)), 0, 0);
			} else if (len < 17) {
				if (len == 14) {
					return new java.sql.Timestamp(
							getYear(value.substring(0, 4)), Utils
									.parseInt(value.substring(4, 6)) - 1, Utils
									.parseInt(value.substring(6, 8)), Utils
									.parseInt(value.substring(8, 10)), Utils
									.parseInt(value.substring(10, 12)), Utils
									.parseInt(value.substring(12, 14)), 0);
				} else {
					return new java.sql.Timestamp(
							getYear(value.substring(0, 4)), Utils
									.parseInt(value.substring(4, 6)) - 1, Utils
									.parseInt(value.substring(6, 8)), Utils
									.parseInt(value.substring(8, 10)), Utils
									.parseInt(value.substring(10, 12)), Utils
									.parseInt(value.substring(12, 14)),
							getMilliByNano(value.substring(14)));
				}
			} else {
				return new java.sql.Timestamp(getYear(value.substring(0, 4)),
						Utils.parseInt(value.substring(4, 6)) - 1, Utils
								.parseInt(value.substring(6, 8)), Utils
								.parseInt(value.substring(8, 10)), Utils
								.parseInt(value.substring(10, 12)), Utils
								.parseInt(value.substring(12, 14)),
						getMilliByNano(value.substring(14)));
			}
		}
	}

	/**
	 * 指定文字をjava.sql.Date変換.
	 * 
	 * @param value
	 *            対象の文字列を設定します.
	 * @return java.sql.Date 日付オブジェクトが返却されます.
	 */
	public static final java.sql.Date getDate(String value) {
		if (value == null || (value = value.trim()).length() <= 0) {
			return null;
		}
		java.sql.Date ret = (java.sql.Date) stringDate(0, value);
		if (ret == null) {
			ret = (java.sql.Date) cutDate(0, value);
		}
		return ret;
	}

	/**
	 * 指定文字をjava.sql.Time変換.
	 * 
	 * @param value
	 *            対象の文字列を設定します.
	 * @return java.sql.Time 日付オブジェクトが返却されます.
	 */
	public static final java.sql.Time getTime(String value) {
		if (value == null || (value = value.trim()).length() <= 0) {
			return null;
		}
		java.sql.Time ret = (java.sql.Time) stringDate(1, value);
		if (ret == null) {
			ret = (java.sql.Time) cutDate(1, value);
		}
		return ret;
	}

	/**
	 * 指定文字をjava.sql.Timestamp変換.
	 * 
	 * @param value
	 *            対象の文字列を設定します.
	 * @return java.sql.Timestamp 日付オブジェクトが返却されます.
	 */
	public static final java.sql.Timestamp getTimestamp(String value) {
		if (value == null || (value = value.trim()).length() <= 0) {
			return null;
		}
		java.sql.Timestamp ret = (java.sql.Timestamp) stringDate(2, value);
		if (ret == null) {
			ret = (java.sql.Timestamp) cutDate(2, value);
		}
		return ret;
	}

	/**
	 * Web上の日付フォーマットをTimestampに変換. こんな感じのフォーマット[Wed, 19-Mar-2014 03:55:33
	 * GMT]を解析して 日付フォーマットに変換します.
	 * 
	 * @param timestamp
	 *            変換対象のHTMLタイムスタンプを設定します.
	 * @return Date 変換された時間が返されます.
	 */
	public static final java.sql.Timestamp getWebTimestamp(String value) {
		if (value == null || (value = value.trim()).length() <= 0) {
			return null;
		}
		char c;
		int len = value.length();
		List<String> list = new ArrayList<String>();
		StringBuilder buf = null;
		for (int i = 0; i < len; i++) {
			c = value.charAt(i);
			if (c != ' ' && c != '\t' && c != ',' && c != ':' && c != '-') {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(c);
			} else if (buf != null) {
				list.add(buf.toString());
				buf = null;
			}
		}
		if (buf != null) {
			list.add(buf.toString());
			buf = null;
		}
		len = list.size();
		if (len == 8) {
			Calendar cal = new GregorianCalendar(GMT_TIMEZONE);
			cal.clear();
			cal.set(Calendar.DAY_OF_MONTH, Utils.parseInt(list.get(1)));
			String month = Utils.toLowerCase(list.get(2));
			if ("jan".equals(month)) {
				cal.set(Calendar.MONTH, 0);
			} else if ("feb".equals(month)) {
				cal.set(Calendar.MONTH, 1);
			} else if ("mar".equals(month)) {
				cal.set(Calendar.MONTH, 2);
			} else if ("apr".equals(month)) {
				cal.set(Calendar.MONTH, 3);
			} else if ("may".equals(month)) {
				cal.set(Calendar.MONTH, 4);
			} else if ("jun".equals(month)) {
				cal.set(Calendar.MONTH, 5);
			} else if ("jul".equals(month)) {
				cal.set(Calendar.MONTH, 6);
			} else if ("aug".equals(month)) {
				cal.set(Calendar.MONTH, 7);
			} else if ("sep".equals(month)) {
				cal.set(Calendar.MONTH, 8);
			} else if ("oct".equals(month)) {
				cal.set(Calendar.MONTH, 9);
			} else if ("nov".equals(month)) {
				cal.set(Calendar.MONTH, 10);
			} else if ("dec".equals(month)) {
				cal.set(Calendar.MONTH, 11);
			}
			cal.set(Calendar.YEAR, Utils.parseInt(list.get(3)));
			cal.set(Calendar.HOUR_OF_DAY, Utils.parseInt(list.get(4)));
			cal.set(Calendar.MINUTE, Utils.parseInt(list.get(5)));
			cal.set(Calendar.SECOND, Utils.parseInt(list.get(6)));
			return new java.sql.Timestamp(cal.getTime().getTime());
		}
		throw new ConvertException("不正なwebTimeフォーマット:" + value);
	}
}
