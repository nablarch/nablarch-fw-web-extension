package nablarch.common.web.download;

import nablarch.fw.web.ResourceLocator;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@link FileResponse}のテストクラス。
 */
public class FileResponseTest {

    /** テスト対象 */
    private FileResponse sut;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private BufferedWriter writer;

    private BufferedReader reader;

    private File file;

    @Before
    public void setUp() throws Exception {
        // テスト用のファイル作成
        file = folder.newFile();
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        writer.write("test");
        writer.close();
    }

    /**
     * ファイルのストリームが取得できること。
     * @throws Exception
     */
    @Test
    public void testGetBodyStream() throws Exception {
        sut = new FileResponse(file);
        InputStream in = sut.getBodyStream();

        assertThat("型がFileInputStreamであること", in, instanceOf(FileInputStream.class));

        reader = new BufferedReader(new InputStreamReader(in));
        assertThat("指定したファイルの内容が読み込めること", reader.readLine(), is("test"));
        reader.close();
    }

    /**
     * コンストラクタに{@code null}を指定した場合に例外となること
     * @throws Exception
     */
    @Test
    public void testNullFile() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("file is required.");

        sut = new FileResponse(null);
    }

    /**
     * ストリームを取得する前にファイルが削除されている場合、例外となること。
     * @throws Exception
     */
    @Test
    public void testGetBodyStreamFileNotFound() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectCause(CoreMatchers.<Throwable>instanceOf(FileNotFoundException.class));

        sut = new FileResponse(file);
        file.delete();
        sut.getBodyStream();
    }

    /**
     * 指定したファイルのサイズが取得できること。
     * @throws Exception
     */
    @Test
    public void testGetContentLength() throws Exception {
        sut = new FileResponse(file);
        assertThat("ファイルの内容のサイズが取得できること", sut.getContentLength(), is("4"));
    }

    /**
     * 必ず{@code false}が返却されること
     * @throws Exception
     */
    @Test
    public void testIsBodyEmpty() throws Exception {
        sut = new FileResponse(file);
        assertThat("falseが返却されること", sut.isBodyEmpty(), is(false));

        sut = new FileResponse(folder.newFile());
        assertThat("falseが返却されること", sut.isBodyEmpty(), is(false));
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testGetBodyString() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.getBodyString();
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.toString();
    }

    /**
     * {@code null}が返却されること。
     * @throws Exception
     */
    @Test
    public void testGetContentPath() throws Exception {
        sut = new FileResponse(file);
        assertThat("nullが返却されること", sut.getContentPath(), is(nullValue()));
    }

    /**
     * ファイルが削除されないこと。
     * @throws Exception
     */
    @Test
    public void testCleanupNotDeleted() throws Exception {
        sut = new FileResponse(file);
        sut.cleanup();
        assertThat("ファイルが削除されていないこと", file.exists(), is(true));
    }

    /**
     * ファイルが削除されること。
     * @throws Exception
     */
    @Test
    public void testCleanupDeleted() throws Exception {
        sut = new FileResponse(file, true);
        sut.cleanup();
        assertThat("ファイルが削除されること", file.exists(), is(false));
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testSetBodyStream() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.setBodyStream(new FileInputStream(file));
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testSetContentPathString() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.setContentPath("test");
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testSetContentPathResource() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.setContentPath(ResourceLocator.valueOf("file://test"));
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testWriteString() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.write("test");
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testWriteByte() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.write("test".getBytes());
    }

    /**
     * サポートしていないことを示す例外が発生すること。
     * @throws Exception
     */
    @Test
    public void testWriteByteBuffer() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(is("unsupported."));

        sut = new FileResponse(file);
        sut.write(ByteBuffer.wrap("test".getBytes()));
    }

}