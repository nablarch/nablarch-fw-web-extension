package nablarch.core.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link BinaryUtil}のテストクラス。
 *
 * @author hisaaki sioiri
 */
public class BinaryUtilTest {

    @Test
    public void testConvertToBytes() throws Exception {
        assertThat(BinaryUtil.convertToBytes(null, Charset.forName("utf-8")), is(new byte[]{}));
        assertThat(BinaryUtil.convertToBytes("", Charset.forName("utf-8")), is(new byte[]{}));
        assertThat(BinaryUtil.convertToBytes("1", Charset.forName("utf-8")), is(new byte[]{0x31}));
        assertThat(BinaryUtil.convertToBytes("0x31", Charset.forName("utf-8")), is(new byte[]{0x31}));

        assertThat(BinaryUtil.convertToBytes("1", 1, Charset.forName("utf-8")), is(new byte[]{0x31}));
        assertThat(BinaryUtil.convertToBytes("1", 2, Charset.forName("utf-8")), is(new byte[]{0x31, 0x00}));
        assertThat(BinaryUtil.convertToBytes("0x32", 2, Charset.forName("utf-8")), is(new byte[]{0x32, 0x00}));
    }

    @Test
    public void testConvertToHexString() {
        assertThat(BinaryUtil.convertToHexString(new byte[]{0x31, 0x32}), is("3132"));
        assertThat(BinaryUtil.convertToHexString(null), is("null"));
    }

    @Test
    public void testConvertToHexStringWithPrefix() {
        assertThat(BinaryUtil.convertToHexStringWithPrefix(new byte[]{0x31}), is("0x31"));
        assertThat(BinaryUtil.convertToHexStringWithPrefix(null), is(""));
        assertThat(BinaryUtil.convertToHexStringWithPrefix(new byte[0]), is(""));
    }

    @Test
    public void testConvertHexToBytes() {
        assertThat(BinaryUtil.convertHexToBytes("3132"), is(new byte[]{0x31, 0x32}));
        assertThat(BinaryUtil.convertHexToBytes("1"), is(new byte[]{0x01}));

        try {
            BinaryUtil.convertHexToBytes("hoge");
            fail();

        } catch(Throwable t) {
            assertThat(t.getClass().getName(), is(NumberFormatException.class.getName()));
            // Java 12 で NumberFormatException のメッセージの最後に基数の情報が追加された。
            // Java 12 以上でもテストが通るようにするため、メッセージは前方一致で検証するようにしている。
            assertThat(
                t.getMessage()
              , is(startsWith("invalid hexadecimal expression. [hoge] :For input string: \"ho\""))
            );
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testConvertHexToBytesParamIsNull() {
        BinaryUtil.convertHexToBytes(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testConvertHexToBytesInvalidHex() {
        BinaryUtil.convertHexToBytes("fg");
    }

    @Test
    public void testFillZerosRight() {
        assertThat(BinaryUtil.fillZerosRight(new byte[]{0x01}, 2), is(new byte[]{0x01, 0x00}));
        assertThat(BinaryUtil.fillZerosRight(new byte[0], 2), is(new byte[0]));
        assertThat(BinaryUtil.fillZerosRight(new byte[]{0x01, 0x02, 0x03}, 2), is(new byte[]{0x01, 0x02, 0x03}));
    }

    @Test
    public void testToByteArray() {

        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[]{0x01, 0x02});
        assertThat(BinaryUtil.toByteArray(stream), is(new byte[]{0x01, 0x02}));
    }

    @Test(expected = RuntimeException.class)
    public void testToByteArrayFail() {
        InputStream stream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException();
            }
        };

        BinaryUtil.toByteArray(stream);
    }
}
