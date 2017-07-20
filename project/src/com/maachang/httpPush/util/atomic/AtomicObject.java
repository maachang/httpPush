package com.maachang.httpPush.util.atomic;

import java.util.concurrent.atomic.AtomicReference;

/**
 * アトミックなオブジェクト.
 * 
 * @version 2013/11/08
 * @author masahito suzuki
 * @since fshttp-1.00
 */
public class AtomicObject<T> {
    private final AtomicReference<T> ato = new AtomicReference<T>(null);

    /**
     * コンストラクタ.
     */
    public AtomicObject() {

    }

    /**
     * コンストラクタ.
     * 
     * @param n
     *            初期値を設定します.
     */
    public AtomicObject(T n) {
        while (!ato.compareAndSet(ato.get(), n))
            ;
    }

    /**
     * Object値を取得.
     * 
     * @return T Object値が返されます.
     */
    public T get() {
        return ato.get();
    }

    /**
     * Object値を設定.
     * 
     * @param n
     *            Object値を設定します.
     */
    public void set(T n) {
        while (!ato.compareAndSet(ato.get(), n))
            ;
    }

    /**
     * Object値を設定して、前回の値を返却.
     * 
     * @param n
     *            Object値を設定します.
     * @return T 前回の値が返却されます.
     */
    public T put(T n) {
        T ret;
        while (!ato.compareAndSet((ret = ato.get()), n))
            ;
        return ret;
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
