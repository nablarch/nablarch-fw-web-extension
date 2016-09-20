package nablarch.core.util;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import nablarch.core.util.annotation.Published;

/**
 * バイナリ操作用ユーティリティクラス
 *
 * @author T.Kawasaki
 */
@Published
public final class BinaryUtil {

    /**
     * コンストラクタ。<br/>
     * 本クラスはインスタンス化できない。
     */
    private BinaryUtil() {
    }

    /**
     * 引数で与えられた文字列をバイト列に変換し、引数のバイト長に満たない場合、右側0x00埋めを行う。
     * <p/>
     * 引数が16進数文字列である場合は、16進数をビット列と見なしバイト列に変換して返却する。
     * <p/>
     * 変換処理の仕様は{@link #convertToBytes(String, Charset)}を参照すること。
     * <p/>
     *
     * @param original 文字列
     * @param length   バイト長
     * @param encoding 文字エンコーディング(引数が16進数文字列である場合は使用されない)
     * @return バイト列
     * @throws NumberFormatException 引数が"0x"から開始しており、かつ16進数文字列として成立していない場合
     */
    public static byte[] convertToBytes(String original, int length, Charset encoding) {
        byte[] bytes = convertToBytes(original, encoding);
        return fillZerosRight(bytes, length);
    }

    /**
     * バイト配列を16進数文字列に変換する。
     * <p/>
     * 引数がnullや空文字である場合の挙動は以下となる。
     * <ul>
     *     <li>引数で与えられたバイト列がnullである場合は、"null"という文字列を返却する</li>
     *     <li>引数で与えられたバイト列の長さが0である場合は、空文字を返却する</li>
     * </ul>
     *
     * @param bytes バイト配列
     * @return 16進数文字列
     */
    public static String convertToHexString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder(bytes.length / 2);
        for (byte b : bytes) {
            char[] hex = {Character.forDigit((b >> 4) & 0x0F, 16),
                    Character.forDigit(b & 0x0F, 16)};
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * バイト配列を16進数文字列に変換する。
     * <p/>
     * 変換後の文字列には"0x"が先頭に付加される。
     * <p/>
     * 引数で与えられたバイト列がnullであるか長さが0である場合、空文字を返却する。
     *
     * @param bytes バイト配列
     * @return 16進数文字列
     */
    public static String convertToHexStringWithPrefix(byte[] bytes) {
        String bareValue = convertToHexString(bytes);
        if (bareValue.length() == 0 || bareValue.equals("null")) {
            return "";
        }
        return "0x" + bareValue;
    }


    /**
     * 引数で与えられた文字列をバイト列に変換する。
     * <p/>
     * 引数で与えられた文字列がnullか空の場合は、長さ0のバイト列を返却する。
     * <p/>
     * 引数が16進数として解釈可能な場合((0x[0-9A-F]+)に適合する場合)は以下の処理を行う。
     * <ul>
     *     <li>16進数をビット列と見なし、バイト列に変換して返却する。</li>
     *     <li>
     *         16進数文字列として成立していない場合(original.matches("0x[0-9A-F].")が成立しない場合)
     *         NumberFormatExceptionを送出する。具体例を以下に示す。
     *         <ul>
     *             <li>"0x"のみの文字列</li>
     *             <li>"0xあああ"</li>
     *         </ul>
     *    </li>
     * </ul>
     * 引数が16進数として解釈できない場合は以下の処理を行う。
     * <ul>
     *     <li>文字列全体を引数で指定した文字セットでエンコーディングし、バイト列に変換して返却する</li>
     * </ul>
     * </p>
     *
     * @param original 16進数文字列（0x[0-9A-F]+）
     * @param encoding 文字エンコーディング(引数が16進数文字列である場合は使用されない)
     * @return バイト列
     * @throws NumberFormatException 引数が"0x"から開始しておりかつ16進数文字列として成立していない場合
     */
    public static byte[] convertToBytes(String original, Charset encoding)
            throws NumberFormatException {


        if (StringUtil.isNullOrEmpty(original)) {
            return new byte[0];
        }

        if (!original.startsWith("0x")) {
            // 先頭に"0x"が記述されていない場合、
            // 文字列全体をバイト列に変換する
            return StringUtil.getBytes(original, encoding);
        }

        String hex = original.substring(2);    // 先頭の"0x"削除
        return convertHexToBytes(hex);
    }

    /**
     * 16進数文字列をバイト列に変換する。
     * <p/>
     * 引数の文字列が以下の条件に当てはまる場合{@link NumberFormatException}を送出する。
     * <ul>
     *     <li>文字列がnullや空文字である場合</li>
     *     <li>（[0-9A-F]+）に当てはまらない場合</li>
     *     <li>16進数文字列であるが、先頭が"0x"で開始している場合。
     *         "0x"で開始する16進数文字列を変換する場合は{@link #convertToBytes(String, Charset)}を利用すること。</li>
     * </ul>
     * <p/>
     *
     * @param hexString 16進数文字列（[0-9A-F]+）
     * @return バイト配列
     * @throws NumberFormatException 16進数文字列として成立していない場合、
     *                                文字列がnullか空文字である場合
     */
    public static byte[] convertHexToBytes(String hexString) {

        if (StringUtil.isNullOrEmpty(hexString)) {
            throw new NumberFormatException("invalid hexadecimal expression. ["
                                                    + hexString + "]");
        }
        int length = hexString.length();
        // 16進数文字列が奇数桁の場合、先頭に0を付加する
        if (length % 2 != 0) {
            hexString = "0" + hexString;
            length++;
        }
        byte[] result = new byte[length / 2];
        String hex;
        for (int i = 0; i < length / 2; i++) {
            int startIndex = i * 2;
            hex = hexString.substring(startIndex, startIndex + 2);
            try {
                result[i] = (byte) Integer.parseInt(hex, 16);
            } catch (NumberFormatException e) {
                throw new NumberFormatException(
                    "invalid hexadecimal expression. [" + hexString + "] :"
                  + e.getMessage() // 例外チェインコンストラクタが無いのでメッセージを連結
                );
            }
        }
        return result;
    }

    /**
     * 右側0詰めを行う。<br/>
     * ただし、以下の場合は0詰めを行わない。
     * <ul>
     * <li>元データ(orig)が0バイトの場合は、0バイトのバイト配列が返却される。</li>
     * <li>元データ(orig)の要素数が、バイト長(length)以上の場合、元データがそのまま返却される。</li>
     * </ul>
     *
     * @param orig   元データ
     * @param length バイト長
     * @return 0詰め後のバイト列
     */
    public static byte[] fillZerosRight(byte[] orig, int length) {

        if ((orig.length == 0) || (orig.length >= length)) {
            return orig;
        }

        byte[] result = new byte[length];
        System.arraycopy(orig, 0, result, 0, orig.length);
        return result;
    }

    /**
     * 入力ストリームをバイト配列に変換する。<br/>
     * 引数であたえられた入力ストリームはクローズされる。
     *
     * @param inputStream 入力ストリーム
     * @return バイト配列
     */
    public static byte[] toByteArray(InputStream inputStream) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new BufferedInputStream(inputStream);
        int b;
        try {
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtil.closeQuietly(in);
        }
        return out.toByteArray();
    }
}
