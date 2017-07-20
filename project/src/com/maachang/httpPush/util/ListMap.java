package com.maachang.httpPush.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ListMapオブジェクト.
 */
public class ListMap {
    private List<String[]> list = new ArrayList<String[]>();

    public ListMap() {
    }

    public ListMap(Map<String, String> v) {
        set(v);
    }

    public ListMap(String... args) {
        set(args);
    }

    private int getNo(String name) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (name.equals(((String[]) list.get(i))[0])) {
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        list.clear();
    }

    public void set(String... args) {
        if (args == null) {
            return;
        }
        int len = args.length;
        for (int i = 0; i < len; i += 2) {
            put(args[i], args[i + 1]);
        }
    }

    public void set(Map<String, String> v) {
        if (v == null) {
            return;
        }
        String k;
        Iterator<String> it = v.keySet().iterator();
        while (it.hasNext()) {
            k = it.next();
            put(k, v.get(k));
        }
    }

    public void put(String key, String value) {
        int no = getNo(key);
        if (no == -1) {
            list.add(new String[] { key, value });
        } else {
            ((String[]) list.get(no))[1] = value;
        }
    }

    public String get(String key) {
        int no = getNo(key);
        if (no == -1) {
            return null;
        }
        return ((String[]) list.get(no))[1];
    }

    public void remove(String key) {
        int no = getNo(key);
        if (no != -1) {
            list.remove(no);
        }
    }

    public int size() {
        return list.size();
    }

    public String[] names() {
        int len = list.size();
        String[] ret = new String[len];
        for (int i = 0; i < len; i++) {
            ret[i] = ((String[]) list.get(i))[0];
        }
        return ret;
    }
}
