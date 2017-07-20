package com.maachang.httpPush.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * バイナリバッファ.
 */
public final class ByteArrayIO extends OutputStream {

    /** デフォルトデータ長. **/
    private static final int MIN_LENGTH = 256;
    private static final int DEF_LENGTH = 512;

    /** Link情報. **/
    private final class BByteLinked {
        byte[] value;
        BByteLinked next;
    };

    // Linkが保持するバイナリ長.
    private int maxBuffer;

    // Link情報.
    private BByteLinked last;
    private BByteLinked first;

    // 書き込み情報長.
    private int useLength;

    // BByteLinkedのLimit値.
    private int limit;

    // 読み込みポジション.
    private int position;

    // クローズフラグ.
    private boolean closeFlag;

    // クローズ時に情報削除を行うフラグ.
    private boolean closeToCleanFlag;

    /**
     * コンストラクタ.
     */
    public ByteArrayIO() {
        this(DEF_LENGTH, false);
    }

    /**
     * コンストラクタ.
     * 
     * @param closeToClean
     *            [true]を設定した場合、クローズ処理時に情報も破棄します.
     */
    public ByteArrayIO(boolean closeToClean) {
        this(DEF_LENGTH, closeToClean);
    }

    /**
     * コンストラクタ.
     * 
     * @param size
     *            対象の１データのバッファ長を設定します.
     */
    public ByteArrayIO(int size) {
        this(size, false);
    }

    /**
     * コンストラクタ.
     * 
     * @param size
     *            対象の１データのバッファ長を設定します.
     * @param closeToClean
     *            [true]を設定した場合、クローズ処理時に情報も破棄します.
     */
    public ByteArrayIO(int size, boolean closeToClean) {
        if (size <= MIN_LENGTH) {
            maxBuffer = MIN_LENGTH;
        } else {
            maxBuffer = size;
        }
        last = new BByteLinked();
        last.value = new byte[maxBuffer];
        last.next = null;

        first = last;

        useLength = 0;
        limit = 0;
        position = 0;
        closeFlag = false;
        closeToCleanFlag = closeToClean;
    }

    /**
     * 情報クリア.
     */
    public void clear() {
        first = last;
        useLength = 0;
        limit = 0;
        position = 0;
        closeFlag = false;
    }

    /**
     * 情報クローズ.
     * 
     * @exception IOException
     *                例外.
     */
    public void close() throws IOException {
        closeFlag = true;

        // クローズ後に、情報も併せて削除指定されている場合.
        if (closeToCleanFlag) {
            first = null;
            last = null;
            useLength = 0;
            limit = 0;
            position = 0;
        }
    }

    /**
     * フラッシュ.
     * 
     * @exception IOException
     *                例外.
     */
    public void flush() throws IOException {
        // なし.
    }

    /**
     * データセット.
     * 
     * @param b
     *            対象のバイナリ情報を設定します.
     * @exception IOException
     *                例外.
     */
    public void write(int b) throws IOException {

        if (closeFlag) {
            throw new IOException("既にクローズされています");
        }

        // 書き込みバッファがいっぱいの場合.
        if (limit >= maxBuffer) {

            // 新しい領域を作成.
            last.next = new BByteLinked();
            last = last.next;
            last.value = new byte[maxBuffer];
            last.next = null;
            limit = 0;
        }
        last.value[limit++] = (byte) b;
        useLength++;
    }

    /**
     * データセット.
     * 
     * @param bin
     *            対象のバイナリを設定します.
     * @exception IOException
     *                例外.
     */
    public void write(byte[] bin) throws IOException {
        write(bin, 0, bin.length);
    }

    /**
     * データセット.
     * 
     * @param bin
     *            対象のバイナリを設定します.
     * @param off
     *            対象のオフセット値を設定します.
     * @param len
     *            対象のデータ長を設定します.
     * @exception IOException
     *                例外.
     */
    public void write(byte[] bin, int off, int len) throws IOException {

        if (closeFlag) {
            throw new IOException("既にクローズされています");
        } else if (len <= 0) {
            return;
        }

        // 現在の書き込み位置を含む、1つのBByteLinked以上の情報長の場合.
        if (len + limit > maxBuffer) {
            int n;
            int bLen = len;
            while (true) {
                // バッファに出力可能.
                if (maxBuffer > limit + len) {
                    System.arraycopy(bin, off, last.value, limit, len);
                    useLength += bLen;
                    limit += len;
                    return;
                }

                // バッファをオーバーする.
                System.arraycopy(bin, off, last.value, limit, (n = maxBuffer
                        - limit));
                off += n;
                len -= n;

                // 新しい領域を作成.
                last.next = new BByteLinked();
                last = last.next;
                last.value = new byte[maxBuffer];
                last.next = null;
                limit = 0;
            }
        }
        // 条件が収まる場合.
        else if (len > 0) {
            System.arraycopy(bin, off, last.value, limit, len);
            useLength += len;
            limit += len;
        }
    }

    /**
     * データセット.
     * 
     * @param 対象のByteBufferを設定します
     *            .
     * @exception IOException
     *                例外.
     */
    public void write(ByteBuffer buf) throws IOException {

        if (closeFlag) {
            throw new IOException("既にクローズされています");
        }

        int len = buf.remaining();
        if (len <= 0) {
            return;
        }

        // 現在の書き込み位置を含む、1つのBByteLinked以上の情報長の場合.
        if (len + limit > maxBuffer) {
            int n;
            int bLen = len;
            while (true) {
                // バッファに出力可能.
                if (maxBuffer > limit + len) {
                    buf.get(last.value, limit, len);
                    useLength += bLen;
                    limit += len;
                    return;
                }

                // バッファをオーバーする.
                buf.get(last.value, limit, (n = maxBuffer - limit));
                len -= n;

                // 新しい領域を作成.
                last.next = new BByteLinked();
                last = last.next;
                last.value = new byte[maxBuffer];
                last.next = null;
                limit = 0;
            }
        }
        // 条件が収まる場合.
        else if (len > 0) {
            buf.get(last.value, limit, len);
            useLength += len;
            limit += len;
        }

    }

    /**
     * 現在の書き込みバッファ長を取得.
     * 
     * @return int 書き込みバッファ長が返却されます.
     */
    public int writeLength() {
        return useLength;
    }

    /**
     * @return int 書き込みバッファ長が返却されます.
     */
    public int size() {
        return useLength;
    }

    /**
     * クローズ処理が行われている場合.
     * 
     * @return boolean [true]の場合、既にクローズ処理が行われています.
     */
    public boolean isClose() {
        return closeFlag;
    }

    /**
     * 情報の参照取得. ※この処理では、参照取得されるだけで、ポジション移動はしません.
     * 
     * @param buf
     *            対象のバッファ情報を設定します.
     * @return int 取得された情報長が返却されます.
     * @exception IOException
     *                例外.
     */
    public int peek(byte[] buf) throws IOException {
        return peek(buf, 0, buf.length);
    }

    /**
     * 情報の参照取得. ※この処理では、参照取得されるだけで、ポジション移動はしません.
     * 
     * @param buf
     *            対象のバッファ情報を設定します.
     * @param off
     *            対象のオフセット値を設定します.
     * @param len
     *            対象の長さを設定します.
     * @return int 取得された情報長が返却されます.
     * @exception IOException
     *                例外.
     */
    public int peek(byte[] buf, int off, int len) throws IOException {
        if (useLength == 0) {
            if (closeFlag) {
                return -1;
            }
            return 0;
        }
        if (len <= 0) {
            return 0;
        } else if (len > useLength) {
            len = useLength;
        }
        int ret = len;
        BByteLinked n = first;

        if (position > 0) {
            int tLen = maxBuffer - position;
            if (len > tLen) {
                System.arraycopy(n.value, position, buf, off, tLen);
                len -= tLen;
                off += tLen;
                n = n.next;
            } else {
                System.arraycopy(n.value, position, buf, off, len);
                return ret;
            }
        }
        while (len > 0) {
            if (len > maxBuffer) {
                System.arraycopy(n.value, 0, buf, off, maxBuffer);
                len -= maxBuffer;
                off += maxBuffer;
                n = n.next;
            } else {
                System.arraycopy(n.value, 0, buf, off, len);
                return ret;
            }
        }
        return 0;
    }

    /**
     * 情報の取得.
     * 
     * @param buf
     *            対象のバッファ情報を設定します.
     * @return int 取得された情報長が返却されます.
     * @exception IOException
     *                例外.
     */
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * 情報の取得.
     * 
     * @param buf
     *            対象のバッファ情報を設定します.
     * @param off
     *            対象のオフセット値を設定します.
     * @param len
     *            対象の長さを設定します.
     * @return int 取得された情報長が返却されます.
     * @exception IOException
     *                例外.
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        if (useLength == 0) {
            if (closeFlag) {
                return -1;
            }
            return 0;
        }
        if (len <= 0) {
            return 0;
        } else if (len > useLength) {
            len = useLength;
        }
        int ret = len;
        BByteLinked n = first;

        if (position > 0) {
            int tLen = maxBuffer - position;
            if (len > tLen) {
                System.arraycopy(n.value, position, buf, off, tLen);
                len -= tLen;
                off += tLen;
                n = n.next;
            } else {
                System.arraycopy(n.value, position, buf, off, len);
                first = n;
                useLength -= ret;
                position += len;
                return ret;
            }
        }
        while (len > 0) {
            if (len > maxBuffer) {
                System.arraycopy(n.value, 0, buf, off, maxBuffer);
                len -= maxBuffer;
                off += maxBuffer;
                n = n.next;
            } else {
                System.arraycopy(n.value, 0, buf, off, len);
                first = n;
                useLength -= ret;
                position = len;
                return ret;
            }
        }
        return 0;
    }

    /**
     * 情報の取得.
     * 
     * @param buf
     *            対象のByteBufferを設定します.
     * @return int 取得された情報長が返却されます.
     */
    public int read(ByteBuffer buf) throws Exception {
        if (useLength == 0) {
            if (closeFlag) {
                return -1;
            }
            return 0;
        }
        int len = buf.remaining();
        if (len <= 0) {
            return 0;
        } else if (len > useLength) {
            len = useLength;
        }
        int ret = len;
        BByteLinked n = first;

        if (position > 0) {
            int tLen = maxBuffer - position;
            if (len > tLen) {
                buf.put(n.value, position, tLen);
                len -= tLen;
                n = n.next;
            } else {
                buf.put(n.value, position, len);
                first = n;
                useLength -= ret;
                position += len;
                return ret;
            }
        }
        while (len > 0) {
            if (len > maxBuffer) {
                buf.put(n.value, 0, maxBuffer);
                len -= maxBuffer;
                n = n.next;
            } else {
                buf.put(n.value, 0, len);
                first = n;
                useLength -= ret;
                position = len;
                return ret;
            }
        }
        return 0;
    }

    /**
     * データスキップ.
     * 
     * @parma len スキップするデータ長を設定します.
     * @return int 実際にスキップされた数が返却されます. [-1]が返却された場合、オブジェクトはクローズしています.
     */
    public final int skip(int len) {
        if (useLength == 0) {
            if (closeFlag) {
                return -1;
            }
            return 0;
        }
        if (len <= 0) {
            return 0;
        } else if (len > useLength) {
            len = useLength;
        }
        int ret = len;
        BByteLinked n = first;

        if (position > 0) {
            int tLen = maxBuffer - position;
            if (len > tLen) {
                len -= tLen;
                n = n.next;
            } else {
                first = n;
                useLength -= ret;
                position += len;
                return ret;
            }
        }
        while (len > 0) {
            if (len > maxBuffer) {
                len -= maxBuffer;
                n = n.next;
            } else {
                first = n;
                useLength -= ret;
                position = len;
                return ret;
            }
        }
        return 0;
    }

    /**
     * データ取得.
     * 
     * @return byte[] 設定されているデータを全て取得します.
     */
    public byte[] toByteArray() {
        int tLen;
        int len = useLength;
        int off = 0;
        BByteLinked n = first;
        byte[] ret = new byte[len];

        if (position > 0) {
            System.arraycopy(n.value, position, ret, off,
                    (tLen = (len > maxBuffer - position) ? maxBuffer - position
                            : len));
            len -= tLen;
            off += tLen;
            n = n.next;
        }
        while (len > 0) {
            System.arraycopy(n.value, 0, ret, off,
                    (tLen = (len > maxBuffer) ? maxBuffer : len));
            len -= tLen;
            off += tLen;
            n = n.next;
        }
        return ret;
    }

    /**
     * 対象OutputStreamに、現在のデータを出力. データは全削除されます.
     * 
     * @param o
     *            対象のOutputStreamを設定します.
     * @exception Exception
     *                例外.
     */
    public void outputStream(OutputStream o) throws Exception {
        int len = (first == last) ? useLength : maxBuffer;
        while (len != 0) {
            o.write(pop(), position, len - position);
            len = (first == last) ? useLength : maxBuffer;
        }
    }

    /**
     * 現在の読み込みポジションを取得.
     * 
     * @return int 現在の読み込みポジションが返却されます.
     */
    public int getPosition() {
        return position;
    }

    /**
     * 一番上のバイナリサイズを取得.
     * 
     * @return int 一番上のバイナリサイズを取得します.
     */
    public int getFirstLength() {
        return (first == last) ? useLength : maxBuffer;
    }

    /**
     * 一番上のバイナリ情報を取得. バイナリが取得された場合は、その情報は削除されます.
     * 
     * @return byte[] 一番上のバイナリ情報が返却されます.
     */
    public byte[] pop() {
        byte[] b = first.value;
        if (first == last) {
            useLength = 0;
            position = 0;
            limit = 0;
        } else {
            useLength -= (maxBuffer - position);
            position = 0;
            first = first.next;
        }
        return b;
    }

    /**
     * 指定条件の位置を取得.
     * 
     * @param chk
     *            チェック対象のバイナリ情報を設定します.
     * @return int 取得データ長が返却されます. [-1]の場合は情報は存在しません.
     */
    public final int indexOf(final byte[] chk) {
        BByteLinked nsrc;
        byte[] nbin;
        BByteLinked src = first;
        byte[] bin = src.value;
        int bLen = bin.length;

        int p, pp, n, len, j, cLen;
        p = position;
        len = useLength;
        cLen = chk.length;

        if (cLen == 1) {
            byte f = chk[0];
            for (int i = 0; i < len; i++, p++) {
                if (p >= bLen) {
                    src = src.next;
                    bin = src.value;
                    p = 0;
                }
                if (f == bin[p]) {
                    return i;
                }
            }
        } else {
            byte f = chk[0];
            for (int i = 0; i < len; i++, p++) {
                if (p >= bLen) {
                    src = src.next;
                    bin = src.value;
                    p = 0;
                }
                if (f == bin[p]) {
                    for (j = i, n = 1, nsrc = src, nbin = bin, pp = p + 1; j < len; j++, pp++) {
                        if (pp >= bLen) {
                            nsrc = nsrc.next;
                            nbin = nsrc.value;
                            pp = 0;
                        }
                        if (chk[n++] != nbin[pp]) {
                            break;
                        } else if (n == cLen) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 検索一致条件までの情報を取得.
     * 
     * @param buf
     *            設定対象のバイナリ情報を設定します.
     * @param off
     *            設定対象のオフセット値を設定します.
     * @param chk
     *            チェック対象のバイナリ情報を設定します.
     * @return int 取得データ長が返却されます. [-1]の場合は情報は存在しません.
     *         [-2]の場合は、情報が大きすぎて、設定対象のバイナリ情報に格納できません.
     * @exception Exception
     *                例外.
     */
    public final int search(final byte[] buf, int off, final byte[] chk)
            throws Exception {

        BByteLinked nsrc;
        byte[] nbin;
        BByteLinked src = first;
        byte[] bin = src.value;
        int bLen = bin.length;

        int p, pp, n, len, j, cLen;
        p = position;
        len = useLength;
        cLen = chk.length;

        if (cLen == 1) {
            byte f = chk[0];
            for (int i = 0; i < len; i++, p++) {
                if (p >= bLen) {
                    src = src.next;
                    bin = src.value;
                    p = 0;
                }
                if (f == bin[p]) {
                    if (buf.length > i + 1 + off) {
                        return read(buf, off, i + 1);
                    }
                    return -2;
                }
            }
        } else {
            byte f = chk[0];
            for (int i = 0; i < len; i++, p++) {
                if (p >= bLen) {
                    src = src.next;
                    bin = src.value;
                    p = 0;
                }
                if (f == bin[p]) {
                    for (j = i, n = 1, nsrc = src, nbin = bin, pp = p + 1; j < len; j++, pp++) {
                        if (pp >= bLen) {
                            nsrc = nsrc.next;
                            nbin = nsrc.value;
                            pp = 0;
                        }
                        if (chk[n++] != nbin[pp]) {
                            break;
                        } else if (n == cLen) {
                            if (buf.length > i + cLen + off) {
                                return read(buf, off, i + cLen);
                            }
                            return -2;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * データが存在するかチェック.
     * 
     * @return boolean [true]の場合、空です.
     */
    public boolean isEmpty() {
        return useLength == 0;
    }

}
