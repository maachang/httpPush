package com.maachang.httpPush.util.shutdown;

import com.maachang.httpPush.util.atomic.AtomicNumber32;

/**
 * シャットダウンコールバック定義. サーバーシャットダウンを実行するコールバック処理をこのオブジェクトを
 * 継承してShutdownHook.registHook()に登録することで、シャットダウン時に 処理が実行されます.
 */
public abstract class CallbackShutdown {

    /**
     * シャットダウン実行フラグ.
     */
    protected final AtomicNumber32 isShutdown = new AtomicNumber32(0);

    /**
     * シャットダウン実行フラグを取得. <BR>
     * 
     * @return boolean [true]の場合、既に実行されました.
     */
    public boolean isShutdown() {
        return isShutdown.get() != 0;
    }

    /**
     * シャットダウン完了フラグを設定.
     */
    protected void exitShutdown() {
        isShutdown.set(1);
    }

    /**
     * シャットダウンを実行するコールバックメソッド. ※利用する場合はこのメソッドを実装してください.
     */
    public abstract void execution();

}
