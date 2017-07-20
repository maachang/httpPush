package com.maachang.httpPush.util.atomic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 待機、復帰処理.
 */
public class Wait {

    // ロックオブジェクト.
    private final Lock sync = new ReentrantLock();
    private final Condition con = sync.newCondition();
    private final AtomicNumber32 awaitFlag = new AtomicNumber32(0);

    /**
     * コンストラクタ.
     */
    public Wait() {
    }

    /**
     * 指定時間待機.
     * 
     * @exception Exception
     *                例外.
     */
    public final void await() throws Exception {
        sync.lock();
        try {
            awaitFlag.inc(); // セット.
            con.await();
        } finally {
            sync.unlock();
            awaitFlag.dec(); // 解除.
        }
    }

    /**
     * 指定時間待機.
     * 
     * @param timeout
     *            ミリ秒での待機時間を設定します. [0]を設定した場合、無限待機となります.
     * @return boolean [true]が返された場合、復帰条件が設定されました.
     * @exception Exception
     *                例外.
     */
    public final boolean await(long time) throws Exception {
        if (time <= 0L) {
            await();
            return true;
        } else {
            sync.lock();
            try {
                awaitFlag.inc(); // セット.
                return con.await(time, TimeUnit.MILLISECONDS);
            } finally {
                sync.unlock();
                awaitFlag.dec(); // 解除.
            }
        }
    }

    /**
     * 待機中のスレッドを１つ起動.
     * 
     * @exception Exception
     *                例外.
     */
    public final void signal() throws Exception {
        if (awaitFlag.get() > 0) {
            sync.lock();
            try {
                con.signal();
            } finally {
                sync.unlock();
            }
        }
    }

    /**
     * 待機中のスレッドを全て起動.
     * 
     * @exception Exception
     *                例外.
     */
    public final void signalAll() throws Exception {
        if (awaitFlag.get() > 0) {
            sync.lock();
            try {
                con.signalAll();
            } finally {
                sync.unlock();
            }
        }
    }

    /**
     * 現在待機中かチェック.
     * 
     * @return boolean [true]の場合、待機中です.
     */
    public final boolean isWait() {
        return awaitFlag.get() > 0;
    }

    /**
     * ロックオブジェクトの取得.
     * 
     * @return Lock ロックオブジェクトが返却されます.
     */
    public final Lock getLock() {
        return sync;
    }

}
