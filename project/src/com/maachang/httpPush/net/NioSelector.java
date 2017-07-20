package com.maachang.httpPush.net;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public final class NioSelector {

    /** Selector. **/
    private Selector selector = null;

    /** Setオブジェクト. **/
    private Set<SelectionKey> selecterSet = null;

    /** wakeupモード. **/
    private volatile boolean wakeupFlag = false;

    /**
     * コンストラクタ.
     * 
     * @exception Exception
     *                例外.
     */
    public NioSelector() throws Exception {
        selector = Selector.open();
        selecterSet = selector.selectedKeys();
    }

    /**
     * オブジェクトクローズ.
     */
    public final void close() {
        final Selector s = selector;
        selector = null;
        selecterSet = null;
        if (s != null) {
            try {
                Iterator<SelectionKey> it = s.keys().iterator();
                while (it.hasNext()) {
                    try {
                        NioUtil.destroyKey(it.next());
                    } catch (Throwable tt) {
                    }
                }
            } catch (Throwable t) {
            }
            try {
                s.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * リセット処理.
     */
    protected final void reset() {
        selecterSet = selector.selectedKeys();
        wakeupFlag = false;
    }

    /**
     * 待機処理.
     * 
     * @return [true]の場合、情報が存在します.
     * @exception Exception
     *                例外.
     */
    public final boolean select() throws Exception {
        if (selecterSet.size() != 0) {
            wakeupFlag = false;
            // Thread.yield() ;
            selector.selectNow();
            return true;
        }

        // Wakeup処理が呼び出された場合.
        if (wakeupFlag) {
            wakeupFlag = false;
            // Thread.yield() ;
            return selector.selectNow() != 0;
        }

        // 通信待機.
        // Thread.yield() ;
        return selector.select() != 0;
    }

    /**
     * 待機処理.
     * 
     * @param timeout
     *            タイムアウト値を設定します.
     * @return [true]の場合、情報が存在します.
     * @exception Exception
     *                例外.
     */
    public final boolean select(final int timeout) throws Exception {
        if (selecterSet.size() != 0) {
            wakeupFlag = false;
            // Thread.yield() ;
            selector.selectNow();
            return true;
        }

        // Wakeup処理が呼び出された場合.
        if (wakeupFlag) {
            wakeupFlag = false;
            // Thread.yield() ;
            return selector.selectNow() != 0;
        }

        // 通信待機.
        // Thread.yield() ;
        return selector.select(timeout) != 0;
    }

    /**
     * 未処理件数を取得.
     * 
     * @return boolean [true]の場合、未処理件数が存在します.
     */
    public final boolean isData() {
        return selecterSet.size() != 0;
    }

    /**
     * 書き込み処理等のWakeup処理. この処理は[select]で待機中以外は、wakeupを呼び出しません.
     */
    public final void wakeup() {

        // select呼び出し以降に、
        // wakeupが行われていない場合は
        // wakeup実行.
        if (!wakeupFlag) {
            wakeupFlag = true;
            selector.wakeup();
        }
    }

    /**
     * Wakeup状態に変更. 実際にはWakeupは呼び出さず、モードだけを変更します.
     */
    public final void changeWakeup() {

        // selectNowを呼び出すフラグのみをセット.
        wakeupFlag = true;
    }

    /**
     * 書き込み処理等のWakeup処理したかチェック.
     * 
     * @return boolean [true]の場合、Wakeupしています.
     */
    public final boolean isWakeup() {
        return wakeupFlag;
    }

    /**
     * selectedKeysを取得.
     * 
     * @return Set<SelectionKey> Setオブジェクトが返却されます.
     */
    public final Set<SelectionKey> selectedKeys() {
        return selecterSet;
    }

    /**
     * Iteratorを取得.
     * 
     * @return Iterator<SelectionKey> Iteratorが返却されます.
     */
    public final Iterator<SelectionKey> iterator() {
        return selecterSet.iterator();
    }

    /**
     * 登録されているSelectionKey情報を取得.
     * 
     * @return Iterator<SelectionKey> Iteratorが返却されます.
     */
    public final Iterator<SelectionKey> regKeys() {
        return selector.keys().iterator();
    }

    /**
     * チャネル登録.
     * 
     * @param channel
     *            登録対象のチャネルを設定します.
     * @param op
     *            対象のオプションを設定します.
     * @return SelectionKey 登録されたキー情報が返却されます.
     * @exception Exception
     *                例外.
     */
    public final SelectionKey register(final SelectableChannel channel,
            final int op) throws Exception {
        return register(channel, op, null);
    }

    /**
     * チャネル登録.
     * 
     * @param channel
     *            登録対象のチャネルを設定します.
     * @param op
     *            対象のオプションを設定します.
     * @param obj
     *            キー登録するオブジェクトを設定します.
     * @return SelectionKey 登録されたキー情報が返却されます.
     * @exception Exception
     *                例外.
     */
    public final SelectionKey register(final SelectableChannel channel,
            final int op, final Object obj) throws Exception {
        return channel.register(selector, op, obj);
    }

}
