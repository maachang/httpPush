package com.maachang.httpPush.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * iniデータ解析用オブジェクト.
 */
public class ReadIni {

    /**
     * INIデータ解析.
     * 
     * @param param
     *            解析対象の情報を設定します.
     * @param index
     *            解析元の情報を設定します.
     * @exception Exception
     *                例外.
     */
    public static final void analisys(Config param, BufferedReader index)
            throws Exception {
        analisys(param, "", index);
    }

    /**
     * INIデータ解析.
     * 
     * @param param
     *            解析対象の情報を設定します.
     * @param header
     *            セクション名の前に付加する内容を設定します.
     * @param index
     *            解析元の情報を設定します.
     * @exception Exception
     *                例外.
     */
    public static final void analisys(Config param, String header,
            BufferedReader index) throws Exception {
        analisysConf(param, header, index);
    }

    /** 新コンフィグ解析処理. **/
    private static final void analisysConf(Config param, String header,
            BufferedReader buf) throws Exception {
        List<String> list = new ArrayList<String>();
        // 全内容を改行単位でリストに取得.
        {
            String s;
            while (true) {
                if ((s = buf.readLine()) == null) {
                    break;
                }
                list.add(s);
            }
            buf.close();
        }
        // コメントをはずす.
        {
            String s;
            char c;
            int len = list.size();
            int lenJ;
            int cote = -1;
            int bef = -1;
            for (int i = 0; i < len; i++) {
                s = (String) list.get(i);
                lenJ = s.length();
                cote = -1;
                for (int j = 0; j < lenJ; j++) {
                    c = s.charAt(j);
                    if (cote == -1) {
                        if (c == '#') {
                            if (j == 0) {
                                list.set(i, "");
                            } else {
                                list.set(i, s.substring(0, j));
                            }
                            break;
                        } else if (bef != '\\' && (c == '\'' || c == '\"')) {
                            cote = c;
                        }
                    } else if (bef != '\\' && c == cote) {
                        cote = -1;
                    }
                    bef = c;
                }
            }
        }
        // データ解析.
        {
            String s;
            char c;
            int len = list.size();
            int lenJ;
            int cote = -1;
            int par = -1;
            int parCount = 0;
            int bef = -1;
            int type = 0;
            StringBuilder b = null;
            String section = null;
            String key = null;
            String value = null;
            StringBuilder tmpValue = null;
            for (int i = 0; i < len; i++) {
                s = (String) list.get(i);
                lenJ = s.length();
                bef = -1;
                for (int j = 0; j < lenJ; j++) {
                    c = s.charAt(j);
                    switch (type) {
                    case 0:// セクション/キー取得条件.
                    {
                        if (b != null) {
                            b.append(c);
                        }
                        // セクション系情報を取得.
                        if (c == '[') {
                            section = null;
                            b = new StringBuilder();
                        } else if (c == ']') {
                            section = b.toString();
                            section = section
                                    .substring(0, section.length() - 1).trim();
                            b = null;
                        } else if (section == null) {
                            // 情報読み込み中か、空白は無視.
                            if (b != null || c == ' ' || c == '　' || c == '\t') {
                                bef = c;
                                continue;
                            }
                            // セクション情報が取得されていない場合に、
                            // 何らかの文字列が出現した場合は、エラー.
                            throw new IOException(errorMessage(
                                    "セクション情報が定義されていません", j, i, s));
                        }
                        // キー条件を取得.
                        else {
                            if (c == '=') {
                                if (b == null) {
                                    throw new IOException(errorMessage(
                                            "不正な文字列が存在します", j, i, s));
                                } else {
                                    key = b.toString();
                                    key = key.substring(0, key.length() - 1)
                                            .trim();
                                    b = new StringBuilder();
                                    type = 1;// 要素取得処理へ.
                                }
                            }
                            // キー開始の場合.
                            else if (b == null
                                    && ((c >= 'a' && c <= 'z')
                                            || (c >= 'A' && c <= 'Z')
                                            || (c >= '0' && c <= '9')
                                            || c == '$' || c == '@' || c == '_')) {
                                b = new StringBuilder();
                                b.append(c);
                            }
                        }
                    }
                        break;
                    case 1:// 要素取得条件.
                    {
                        b.append(c);
                        // コーテーション終端を検地.
                        if (cote != -1) {
                            if (bef != '\\' && cote == c) {
                                cote = -1;
                                c = 0;
                            }
                        }
                        // 括弧の終端を検地.
                        else if (par != -1) {
                            if ((par == '{' && c == '}')
                                    || (par == '[' && c == ']')
                                    || (par == '(' && c == ')')
                                    || (par == '<' && c == '>')) {
                                parCount--;
                                if (parCount <= 0) {
                                    parCount = 0;
                                    par = -1;
                                }
                            }
                        }
                        // コーテーション検出.
                        if (cote == -1 && (c == '\'' || c == '\"')) {
                            cote = c;
                        }
                        // 括弧検出.
                        else if (cote == -1
                                && (c == '{' || c == '[' || c == '(' || c == '<')) {
                            if (par == -1) {
                                par = c;
                            }
                            if (c == par) {
                                parCount++;
                            }
                        }
                        // 終端条件の場合.
                        if (lenJ <= j + 1
                                || (cote == -1 && par == -1 && c == ';')) {
                            // value定義が存在していて、コーテーションおよび、
                            // 括弧条件が存在しない場合.
                            if (cote == -1 && par == -1) {
                                if (tmpValue != null) {
                                    tmpValue.append(b.toString().trim());
                                    value = tmpValue.toString();
                                    tmpValue = null;
                                } else {
                                    value = b.toString().trim();
                                }
                                // ；での終端設定の場合.
                                if (c == ';') {
                                    value = value.substring(0,
                                            value.length() - 1).trim();
                                }
                            }
                            // １行の終端に位置した状態で、括弧の終端が存在しない場合は、次の行も対象とする.
                            else if (par != -1) {
                                if (tmpValue == null) {
                                    tmpValue = new StringBuilder();
                                }
                                tmpValue.append(b.toString().trim());
                                b = new StringBuilder();
                                cote = -1;
                                break;// 次の行もValue条件で処理.
                            }
                            // value終端条件でない場合.
                            else {
                                bef = c;
                                break;
                            }
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                                value = Utils.downIndentDoubleCote(value);
                            } else if (value.startsWith("\'")
                                    && value.endsWith("\'")) {
                                value = value.substring(1, value.length() - 1);
                                value = Utils.downIndentSingleCote(value);
                            }
                            // @cote条件が存在する場合、その条件内容をダブルコーテーション変換.
                            value = convertAtCote(value);
                            param.put(section, key, value);
                            key = null;
                            value = null;
                            b = null;
                            tmpValue = null;
                            par = -1;
                            parCount = 0;
                            cote = -1;
                            bef = -1;
                            type = 0;// 最初に戻る.
                        }
                    }
                        break;
                    }
                    bef = c;
                }
                // valueが定義されていない場合は、value=""として格納する.
                if (tmpValue == null && type == 1 && section != null
                        && key != null && value == null) {
                    param.put(section, key, "");
                    key = null;
                    value = null;
                    b = null;
                    type = 0;// 最初に戻る.
                }
            }
        }
    }

    /** エラーメッセージを生成 **/
    private static final String errorMessage(String message, int x, int y,
            String line) {
        StringBuilder buf = new StringBuilder();
        buf.append(message).append("(").append(x).append("/").append(y)
                .append(")");
        if (line != null && line.length() > 0) {
            buf.append(":[").append(line).append("]");
        }
        return buf.toString();
    }

    /** @cote条件をダブルコーテーションに変換 **/
    private static final String convertAtCote(String value) {
        if (value.toLowerCase().indexOf("@cote") == -1) {
            return value;
        }
        int len = value.length();
        char c;
        int cote = -1;
        StringBuilder buf = new StringBuilder();
        boolean yen = false;
        for (int i = 0; i < len; i++) {
            c = value.charAt(i);
            if (cote != -1) {
                if (cote == c && !yen) {
                    cote = -1;
                }
            } else {
                if (c == '\"' || c == '\'') {
                    cote = c;
                } else if ('@' == c && i + 5 < len) {
                    boolean atCtFlag = false;
                    if ("@cote".equals(value.substring(i, i + 5).toLowerCase())) {
                        int parLen = 0;
                        int n = -1;
                        int cct = -1;
                        char x;
                        for (int j = i + 5; j < len; j++) {
                            x = value.charAt(j);
                            if (cct != -1) {
                                if (cct == x) {
                                    cct = -1;
                                }
                            } else {
                                if (x == '\"' || x == '\'') {
                                    cct = x;
                                } else if (x == '(') {
                                    if (parLen == 0) {
                                        n = j;
                                    }
                                    parLen++;
                                } else if (x == ')') {
                                    if (parLen == 0) {
                                        break;
                                    }
                                    parLen--;
                                    if (parLen <= 0) {
                                        i = j;
                                        String cstr = value.substring(n + 1, j);
                                        cstr = Utils.upIndentDoubleCote(cstr);
                                        buf.append("\"").append(cstr)
                                                .append("\"");
                                        atCtFlag = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (atCtFlag) {
                            continue;
                        }
                    }
                }
            }
            if (c == '\\') {
                yen = true;
            } else {
                yen = false;
            }
            buf.append(c);
        }
        return buf.toString();
    }
}
