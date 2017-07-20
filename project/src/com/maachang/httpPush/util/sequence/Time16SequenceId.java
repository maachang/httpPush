package com.maachang.httpPush.util.sequence;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Time 16 byte(128bit) シーケンスID発行処理.
 */
public class Time16SequenceId {
    private final AtomicInteger nowId = new AtomicInteger(0);
    private final AtomicLong nowTime = new AtomicLong(-1L);
    private int machineId = 0;

    /**
     * コンストラクタ.
     * 
     * @param id
     *            対象のマシンIDを設定します.
     */
    public Time16SequenceId(int id) {
        machineId = id;
    }

    /**
     * コンストラクタ.
     * 
     * @param id
     *            対象のマシンIDを設定します.
     * @param lastTime
     *            設定した最終時間を設定します.
     * @param lastId
     *            設定した最終IDを設定します.
     */
    public Time16SequenceId(int id, long lastTime, int lastId) {
        nowTime.set(lastTime);
        machineId = id;
        nowId.set(lastId);
    }

    /**
     * コンストラクタ.
     * 
     * @param binary
     *            対象のバイナリを設定します.
     */
    public Time16SequenceId(byte[] binary) {
        set(binary);
    }

    /**
     * コンストラクタ.
     * 
     * @param uuid
     *            対象のUUIDを設定します.
     */
    public Time16SequenceId(String uuid) {
        set(uuid);
    }

    /**
     * シーケンスIDを発行.
     * 
     * @param buf
     *            対象のバッファを設定します.
     */
    public final void get(byte[] buf) {
        get(false, buf);
    }

    /**
     * シーケンスIDを発行.
     * 
     * @return byte[] シーケンスIDが発行されます.
     */
    public final byte[] get() {
        final byte[] ret = new byte[16];
        get(ret);
        return ret;
    }

    /**
     * シーケンスIDを発行.
     * 
     * @return String シーケンスIDが発行されます.
     */
    public final String getUUID() {
        return get(true, null);
    }

    /**
     * 現在発行したシーケンスIDを再取得.
     * 
     * @param buf
     *            対象のバッファを設定します.
     */
    public final void now(byte[] buf) {
        createId(buf, machineId, nowTime.get(), nowId.get());
    }

    /**
     * 現在発行したシーケンスIDを再取得.
     * 
     * @return byte[] シーケンスIDが発行されます.
     */
    public final byte[] now() {
        final byte[] ret = new byte[16];
        now(ret);
        return ret;
    }

    /**
     * 現在発行したシーケンスIDを再取得.
     * 
     * @return String シーケンスIDが発行されます.
     */
    public final String nowUUID() {
        return createId(machineId, nowTime.get(), nowId.get());
    }

    /**
     * シーケンスIDを設定.
     * 
     * @param binary
     *            対象のバイナリを設定します.
     */
    public final void set(byte[] binary) {
        setBinary(binary);
    }

    /**
     * シーケンスIDを設定.
     * 
     * @param uuid
     *            対象のUUIDを設定します.
     */
    public final void set(String uuid) {
        setUUID(uuid);
    }

    /**
     * マシンIDを取得.
     * 
     * @return int 設定されているマシンIDが返却されます.
     */
    public final int getMachineId() {
        return (int) machineId;
    }

    /**
     * ID生成.
     */
    private final String get(boolean mode, byte[] buf) {
        int id;
        long beforeTime, time;
        while (true) {
            id = nowId.get();
            beforeTime = nowTime.get();
            time = System.currentTimeMillis();

            // システム時間が変更された場合.
            if (time != beforeTime) {
                if (id < Integer.MAX_VALUE
                        && nowTime.compareAndSet(beforeTime, time)
                        && nowId.compareAndSet(id, 0)) {
                    if (mode) {
                        return createId(machineId, time, 0);
                    }
                    createId(buf, machineId, time, 0);
                    return null;
                }
            }
            // シーケンスIDインクリメント.
            else if (nowId.compareAndSet(id, id + 1)) {
                if (mode) {
                    return createId(machineId, beforeTime, id + 1);
                }
                createId(buf, machineId, beforeTime, id + 1);
                return null;
            }
        }
    }

    /**
     * バイナリ変換.
     */
    private static final void createId(byte[] out, int machineId, long time,
            int seqId) {
        out[0] = (byte) ((time & 0xff00000000000000L) >> 56L);
        out[1] = (byte) ((time & 0x00ff000000000000L) >> 48L);
        out[2] = (byte) ((time & 0x0000ff0000000000L) >> 40L);
        out[3] = (byte) ((time & 0x000000ff00000000L) >> 32L);
        out[4] = (byte) ((time & 0x00000000ff000000L) >> 24L);
        out[5] = (byte) ((time & 0x0000000000ff0000L) >> 16L);
        out[6] = (byte) ((time & 0x000000000000ff00L) >> 8L);
        out[7] = (byte) ((time & 0x00000000000000ffL) >> 0L);

        out[8] = (byte) ((seqId & 0xff000000) >> 24);
        out[9] = (byte) ((seqId & 0x00ff0000) >> 16);
        out[10] = (byte) ((seqId & 0x0000ff00) >> 8);
        out[11] = (byte) ((seqId & 0x000000ff) >> 0);

        out[12] = (byte) ((machineId & 0xff000000) >> 24);
        out[13] = (byte) ((machineId & 0x00ff0000) >> 16);
        out[14] = (byte) ((machineId & 0x0000ff00) >> 8);
        out[15] = (byte) ((machineId & 0x000000ff) >> 0);
    }

    /**
     * NN16進数字ゼロサプレス.
     */
    private static final String zero2(int no) {
        return no >= 16 ? Integer.toHexString(no) : "0"
                + Integer.toHexString(no);
    }

    /**
     * UUID変換.
     */
    private static final String createId(int machineId, long time, int seqId) {
        return new StringBuilder()
                .append(zero2((int) ((time & 0xff00000000000000L) >> 56L)))
                .append(zero2((int) ((time & 0x00ff000000000000L) >> 48L)))
                .append(zero2((int) ((time & 0x0000ff0000000000L) >> 40L)))
                .append(zero2((int) ((time & 0x000000ff00000000L) >> 32L)))
                .append("-")
                .append(zero2((int) ((time & 0x00000000ff000000L) >> 24L)))
                .append(zero2((int) ((time & 0x0000000000ff0000L) >> 16L)))
                .append("-")
                .append(zero2((int) ((time & 0x000000000000ff00L) >> 8L)))
                .append(zero2((int) ((time & 0x00000000000000ffL) >> 0L)))
                .append("-").append(zero2((int) ((seqId & 0xff000000) >> 24)))
                .append(zero2((int) ((seqId & 0x00ff0000) >> 16))).append("-")
                .append(zero2((int) ((seqId & 0x0000ff00) >> 8)))
                .append(zero2((int) ((seqId & 0x000000ff) >> 0)))
                .append(zero2((int) ((machineId & 0xff000000) >> 24)))
                .append(zero2((int) ((machineId & 0x00ff0000) >> 16)))
                .append(zero2((int) ((machineId & 0x0000ff00) >> 8)))
                .append(zero2((int) ((machineId & 0x000000ff) >> 0)))
                .toString();
    }

    /**
     * バイナリから、データ変換.
     */
    private final void setBinary(byte[] value) {
        nowId.set(uuidToSequenceId(value));
        nowTime.set(uuidToTime(value));
        machineId = uuidToMachineId(value);
    }

    /**
     * UUIDから、データ変換.
     */
    private final void setUUID(String uuid) {
        setBinary(getBytes(uuid));
    }

    /**
     * UUID文字列をバイナリに変換. UUIDが正しいかチェックしたい場合にも利用できます.
     * 
     * @param uuid
     *            対象のUUIDを設定します.
     * @return byte[] 戻り値が存在しない場合(null)は、変換に失敗しました.
     */
    public static final byte[] getBytes(String uuid) {
        if (uuid.length() != 36) {
            return null;
        }
        try {
            return new byte[] { toBinary(uuid, 0), toBinary(uuid, 2),
                    toBinary(uuid, 4), toBinary(uuid, 6),
                    toBinary(uuid, 9), // -
                    toBinary(uuid, 11),
                    toBinary(uuid, 14), // -
                    toBinary(uuid, 16),
                    toBinary(uuid, 19),// -
                    toBinary(uuid, 21),
                    toBinary(uuid, 24),// -
                    toBinary(uuid, 26), toBinary(uuid, 28), toBinary(uuid, 30),
                    toBinary(uuid, 32), toBinary(uuid, 34) };
        } catch (NumberFormatException ne) {
            return null;
        }
    }

    /**
     * UUID(binary)から、時間を取得.
     * 
     * @param value
     * @return
     */
    public static final long uuidToTime(byte[] value) {
        return (((long) value[0] & 0x00000000000000ffL) << 56L)
                | (((long) value[1] & 0x00000000000000ffL) << 48L)
                | (((long) value[2] & 0x00000000000000ffL) << 40L)
                | (((long) value[3] & 0x00000000000000ffL) << 32L)
                | (((long) value[4] & 0x00000000000000ffL) << 24L)
                | (((long) value[5] & 0x00000000000000ffL) << 16L)
                | (((long) value[6] & 0x00000000000000ffL) << 8L)
                | (((long) value[7] & 0x00000000000000ffL) << 0L);
    }

    /**
     * UUID(binary)からシーケンスIDを取得.
     * 
     * @param value
     * @return
     */
    public static final int uuidToSequenceId(byte[] value) {
        return (((int) value[8] & 0x000000ff) << 24)
                | (((int) value[9] & 0x000000ff) << 16)
                | (((int) value[10] & 0x000000ff) << 8)
                | (((int) value[11] & 0x000000ff) << 0);
    }

    /**
     * UUID(binary)からマシンIDを取得.
     * 
     * @param value
     * @return
     */
    public static final int uuidToMachineId(byte[] value) {
        return (((int) value[12] & 0x000000ff) << 24)
                | (((int) value[13] & 0x000000ff) << 16)
                | (((int) value[14] & 0x000000ff) << 8)
                | (((int) value[15] & 0x000000ff) << 0);
    }

    /**
     * 指定文字のバイナリ変換.
     */
    private static final byte toBinary(String value, int p) {
        return (byte) Integer.parseInt(value.substring(p, p + 2), 16);
    }
}
