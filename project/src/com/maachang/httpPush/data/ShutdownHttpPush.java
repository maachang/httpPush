package com.maachang.httpPush.data;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.maachang.httpPush.pref.Def;
import com.maachang.httpPush.reception.Reception;
import com.maachang.httpPush.sse.Sse;
import com.maachang.httpPush.util.shutdown.CallbackShutdown;

/**
 * シャットダウン時のデータ保持処理.
 */
public final class ShutdownHttpPush extends CallbackShutdown {
    private static final Log LOG = LogFactory.getLog(ShutdownHttpPush.class);

    /** manager. **/
    protected PushDataManager manager;

    /** Reception. **/
    protected Reception reception;

    /** Sse. **/
    protected Sse sse;

    public ShutdownHttpPush(Reception reception, Sse sse,
            PushDataManager manager) {
        this.reception = reception;
        this.sse = sse;
        this.manager = manager;
    }

    /**
     * シャットダウンフック：PushData管理情報を保存.
     */
    public final void execution() {
        LOG.info("## start shutdown PushHttp");

        // 各サービス停止.
        reception.stop();
        sse.stop();

        OutputStream out = null;
        try {
            manager.stopFlag.set(1); // データアクセス禁止.
            out = new BufferedOutputStream(new FileOutputStream(
                    Def.HTTP_PUSH_DATA));
            manager.save(out);
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            LOG.error("shutdownError", e);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                }
            }
            LOG.info("## end shutdown PushHttp");
        }
    }

}
