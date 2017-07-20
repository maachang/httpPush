package com.maachang.httpPush.util.atomic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * アトミックな数値(64bit)オブジェクト.
 */
public class AtomicNumber {

    private final AtomicLong ato = new AtomicLong(0L);

    /**
     * コンストラクタ.
     */
    public AtomicNumber() {

    }

    /**
     * コンストラクタ.
     * 
     * @param n
     *            初期値を設定します.
     */
    public AtomicNumber(long n) {
        while (!ato.compareAndSet(ato.get(), n))
            ;
    }

    /**
     * long値を取得.
     * 
     * @return long long値が返されます.
     */
    public long get() {
        return ato.get();
    }

    /**
     * long値を設定.
     * 
     * @param n
     *            long値を設定します.
     */
    public void set(long n) {
        while (!ato.compareAndSet(ato.get(), n))
            ;
    }

    /**
     * long値を設定して前回の値を取得.
     * 
     * @param n
     *            long値を設定します.
     * @return long 前回の値が返却されます.
     */
    public long put(long n) {
        long ret;
        while (!ato.compareAndSet((ret = ato.get()), n))
            ;
        return ret;
    }

    /**
     * 指定数の足し算.
     * 
     * @param no
     *            対象の数値を設定します.
     * @return long 結果内容が返されます.
     */
    public long add(long no) {
        long n, r;
        while (!ato.compareAndSet((n = ato.get()), (r = n + no)))
            ;
        return r;
    }

    /**
     * 指定数の引き算.
     * 
     * @param no
     *            対象の数値を設定します.
     * @return long 結果内容が返されます.
     */
    public long remove(long no) {
        long n, r;
        while (!ato.compareAndSet((n = ato.get()), (r = n - no)))
            ;
        return r;
    }

    /**
     * 1インクリメント.
     * 
     * @return long 結果内容が返されます.
     */
    public long inc() {
        long n, r;
        while (!ato.compareAndSet((n = ato.get()), (r = n + 1L)))
            ;
        return r;
    }

    /**
     * 1デクリメント.
     * 
     * @return long 結果内容が返されます.
     */
    public long dec() {
        long n, r;
        while (!ato.compareAndSet((n = ato.get()), (r = n - 1L)))
            ;
        return r;
    }

    /**
     * 文字変換.
     * 
     * @return String 文字に変換します.
     */
    public String toString() {
        return String.valueOf(ato.get());
    }
}
