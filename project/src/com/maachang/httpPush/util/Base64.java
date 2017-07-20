package com.maachang.httpPush.util;

/**
 * Base64.
 */
public class Base64 {

    /**
     * 無効コード値.
     */
    private static final int _ND = 0x7fffffff;

    /**
     * 余りデコード値.
     */
    private static final byte REMAINDER_ENC = (byte) '=';

    /**
     * エンコード表.
     */
    private static final byte[] ENC_CD = { (byte) 'A', (byte) 'B', (byte) 'C',
            (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H',
            (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M',
            (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R',
            (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W',
            (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b',
            (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
            (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l',
            (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q',
            (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
            (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0',
            (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
            (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+',
            (byte) '/' };

    /**
     * デコード表.
     */
    private static final int[] DEC_CD = { _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, 0x0000003e, _ND,
            _ND, _ND, 0x0000003f, 0x00000034, 0x00000035, 0x00000036,
            0x00000037, 0x00000038, 0x00000039, 0x0000003a, 0x0000003b,
            0x0000003c, 0x0000003d, _ND, _ND, _ND, 0x00000040, _ND, _ND, _ND,
            0x00000000, 0x00000001, 0x00000002, 0x00000003, 0x00000004,
            0x00000005, 0x00000006, 0x00000007, 0x00000008, 0x00000009,
            0x0000000a, 0x0000000b, 0x0000000c, 0x0000000d, 0x0000000e,
            0x0000000f, 0x00000010, 0x00000011, 0x00000012, 0x00000013,
            0x00000014, 0x00000015, 0x00000016, 0x00000017, 0x00000018,
            0x00000019, _ND, _ND, _ND, _ND, _ND, _ND, 0x0000001a, 0x0000001b,
            0x0000001c, 0x0000001d, 0x0000001e, 0x0000001f, 0x00000020,
            0x00000021, 0x00000022, 0x00000023, 0x00000024, 0x00000025,
            0x00000026, 0x00000027, 0x00000028, 0x00000029, 0x0000002a,
            0x0000002b, 0x0000002c, 0x0000002d, 0x0000002e, 0x0000002f,
            0x00000030, 0x00000031, 0x00000032, 0x00000033, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND,
            _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND, _ND };

    /**
     * コンストラクタ.
     */
    private Base64() {
    }

    /**
     * エンコード処理.
     * 
     * @param binary
     *            エンコード対象のバイナリ情報を設定します.
     * @return byte[] Base64にエンコードされたバイナリ情報が返されます.
     */
    public static final byte[] encodeBinary(final byte[] binary) {
        return encodeBinary(binary, 0, binary.length);
    }

    /**
     * エンコード処理.
     * 
     * @param binary
     *            エンコード対象のバイナリ情報を設定します.
     * @param off
     *            対象のオフセット値を設定します.
     * @param length
     *            対象の長さを設定します.
     * @return byte[] Base64にエンコードされたバイナリ情報が返されます.
     */
    public static final byte[] encodeBinary(final byte[] binary, final int off,
            final int length) {
        int i, j, k;
        int len;
        int etc;
        byte[] ary = null;
        etc = length % 3;
        len = length / 3;
        ary = new byte[(len * 4) + ((etc != 0) ? 4 : 0)];
        for (i = 0, j = off, k = 0; i < len; i++, j += 3, k += 4) {
            ary[k] = Base64.ENC_CD[(int) ((binary[j] & 0x000000fc) >> 2)];
            ary[k + 1] = Base64.ENC_CD[(int) (((binary[j] & 0x00000003) << 4) | ((binary[j + 1] & 0x000000f0) >> 4))];
            ary[k + 2] = Base64.ENC_CD[(int) (((binary[j + 1] & 0x0000000f) << 2) | ((binary[j + 2] & 0x000000c0) >> 6))];
            ary[k + 3] = Base64.ENC_CD[(int) (binary[j + 2] & 0x0000003f)];
        }
        switch (etc) {
        case 1:
            j = (len * 3) + off;
            k = len * 4;
            ary[k] = Base64.ENC_CD[(int) ((binary[j] & 0x000000fc) >> 2)];
            ary[k + 1] = Base64.ENC_CD[(int) ((binary[j] & 0x00000003) << 4)];
            ary[k + 2] = Base64.REMAINDER_ENC;
            ary[k + 3] = Base64.REMAINDER_ENC;
            break;
        case 2:
            j = (len * 3) + off;
            k = len * 4;
            ary[k] = Base64.ENC_CD[(int) ((binary[j] & 0x000000fc) >> 2)];
            ary[k + 1] = Base64.ENC_CD[(int) (((binary[j] & 0x00000003) << 4) | ((binary[j + 1] & 0x000000f0) >> 4))];
            ary[k + 2] = Base64.ENC_CD[(int) (((binary[j + 1] & 0x0000000f) << 2))];
            ary[k + 3] = Base64.REMAINDER_ENC;
            break;
        }
        return ary;
    }

    /**
     * エンコード処理.
     * 
     * @param binary
     *            エンコード対象のバイナリ情報を設定します.
     * @return String Base64にエンコードされた文字情報が返されます.
     */
    public static final String encode(final byte[] binary) {
        return encode(binary, 0, binary.length);
    }

    /**
     * エンコード処理.
     * 
     * @param binary
     *            エンコード対象のバイナリ情報を設定します.
     * @param off
     *            対象のオフセット値を設定します.
     * @param len
     *            対象の長さを設定します.
     * @return String Base64にエンコードされた文字情報が返されます.
     */
    public static final String encode(final byte[] binary, final int off,
            final int length) {
        int i, j, k;
        int len;
        int etc;
        char[] ary = null;
        etc = length % 3;
        len = length / 3;
        ary = new char[(len * 4) + ((etc != 0) ? 4 : 0)];
        for (i = 0, j = off, k = 0; i < len; i++, j += 3, k += 4) {
            ary[k] = (char) Base64.ENC_CD[((binary[j] & 0x000000fc) >> 2)];
            ary[k + 1] = (char) Base64.ENC_CD[(((binary[j] & 0x00000003) << 4) | ((binary[j + 1] & 0x000000f0) >> 4))];
            ary[k + 2] = (char) Base64.ENC_CD[(((binary[j + 1] & 0x0000000f) << 2) | ((binary[j + 2] & 0x000000c0) >> 6))];
            ary[k + 3] = (char) Base64.ENC_CD[(binary[j + 2] & 0x0000003f)];
        }
        switch (etc) {
        case 1:
            j = (len * 3) + off;
            k = len * 4;
            ary[k] = (char) Base64.ENC_CD[((binary[j] & 0x000000fc) >> 2)];
            ary[k + 1] = (char) Base64.ENC_CD[((binary[j] & 0x00000003) << 4)];
            ary[k + 2] = (char) Base64.REMAINDER_ENC;
            ary[k + 3] = (char) Base64.REMAINDER_ENC;
            break;
        case 2:
            j = (len * 3) + off;
            k = len * 4;
            ary[k] = (char) Base64.ENC_CD[((binary[j] & 0x000000fc) >> 2)];
            ary[k + 1] = (char) Base64.ENC_CD[(((binary[j] & 0x00000003) << 4) | ((binary[j + 1] & 0x000000f0) >> 4))];
            ary[k + 2] = (char) Base64.ENC_CD[(((binary[j + 1] & 0x0000000f) << 2))];
            ary[k + 3] = (char) Base64.REMAINDER_ENC;
            break;
        }
        return new String(ary);
    }

    /**
     * デコード処理.
     * 
     * @param base64
     *            対象のBase64データを設定します.
     * @return byte[] 変換されたバイナリ情報が返されます.
     */
    public static final byte[] decode(final String base64) {
        return decode(base64, 0, base64.length());
    }

    /**
     * デコード処理.
     * 
     * @param base64
     *            対象のBase64データを設定します.
     * @param off
     *            対象のオフセット値を設定します.
     * @param length
     *            対象の長さを設定します.
     * @return byte[] 変換されたバイナリ情報が返されます.
     */
    public static final byte[] decode(final String base64, final int off,
            final int length) {
        int i, j, k, len, etc;
        byte[] ret = null;
        for (i = off + length - 1, etc = 0; i >= 0; i--) {
            if (base64.charAt(i) == Base64.REMAINDER_ENC) {
                etc++;
            } else {
                break;
            }
        }
        len = length / 4;
        ret = new byte[(len * 3) - etc];
        len -= 1;
        for (i = 0, j = off, k = 0; i < len; i++, j += 4, k += 3) {
            ret[k] = (byte) (((Base64.DEC_CD[base64.charAt(j)] & 0x0000003f) << 2) | ((Base64.DEC_CD[base64
                    .charAt(j + 1)] & 0x00000030) >> 4));
            ret[k + 1] = (byte) (((Base64.DEC_CD[base64.charAt(j + 1)] & 0x0000000f) << 4) | ((Base64.DEC_CD[base64
                    .charAt(j + 2)] & 0x0000003c) >> 2));
            ret[k + 2] = (byte) (((Base64.DEC_CD[base64.charAt(j + 2)] & 0x00000003) << 6) | (Base64.DEC_CD[base64
                    .charAt(j + 3)] & 0x0000003f));
        }
        switch (etc) {
        case 0:
            j = (len * 4) + off;
            k = len * 3;
            ret[k] = (byte) (((Base64.DEC_CD[base64.charAt(j)] & 0x0000003f) << 2) | ((Base64.DEC_CD[base64
                    .charAt(j + 1)] & 0x00000030) >> 4));
            ret[k + 1] = (byte) (((Base64.DEC_CD[base64.charAt(j + 1)] & 0x0000000f) << 4) | ((Base64.DEC_CD[base64
                    .charAt(j + 2)] & 0x0000003c) >> 2));
            ret[k + 2] = (byte) (((Base64.DEC_CD[base64.charAt(j + 2)] & 0x00000003) << 6) | (Base64.DEC_CD[base64
                    .charAt(j + 3)] & 0x0000003f));
            break;
        case 1:
            j = (len * 4) + off;
            k = len * 3;
            ret[k] = (byte) (((Base64.DEC_CD[base64.charAt(j)] & 0x0000003f) << 2) | ((Base64.DEC_CD[base64
                    .charAt(j + 1)] & 0x00000030) >> 4));
            ret[k + 1] = (byte) (((Base64.DEC_CD[base64.charAt(j + 1)] & 0x0000000f) << 4) | ((Base64.DEC_CD[base64
                    .charAt(j + 2)] & 0x0000003c) >> 2));
            break;
        case 2:
            j = (len * 4) + off;
            k = len * 3;
            ret[k] = (byte) (((Base64.DEC_CD[base64.charAt(j)] & 0x0000003f) << 2) | ((Base64.DEC_CD[base64
                    .charAt(j + 1)] & 0x00000030) >> 4));
            break;
        }
        return ret;
    }

}
