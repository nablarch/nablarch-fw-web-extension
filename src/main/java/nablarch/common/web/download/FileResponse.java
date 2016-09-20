package nablarch.common.web.download;

import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.ResourceLocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * {@link File}オブジェクトからHTTPレスポンスを生成する{@link HttpResponse}継承クラス。
 *
 * @author Naoki Yamamoto
 */
public class FileResponse extends HttpResponse {

    /** ダウンロードするファイル */
    private final File file;

    /** リクエスト処理の終了時に自動的にファイルを削除するか否か */
    private final boolean deleteOnCleanup;

    /**
     * コンストラクタ。
     * <p/>
     * 本コンストラクタを使用してインスタンスを生成した場合、
     * リクエスト処理の終了時に自動的にファイルは削除されない。
     *
     * @param file ファイル
     */
    @Published
    public FileResponse(File file) {
        this(file, false);
    }

    /**
     * コンストラクタ。
     *
     * @param file ファイル
     * @param deleteOnCleanup リクエスト処理の終了時に自動的にファイルを削除する場合は{@code true}
     */
    @Published
    public FileResponse(File file, boolean deleteOnCleanup) {
        if (file == null) {
            throw new IllegalArgumentException("file is required.");
        }
        this.file = file;
        this.deleteOnCleanup = deleteOnCleanup;
        setHeader("Content-Length", String.valueOf(file.length()));
    }

    @Override
    public String getContentLength() {
        return getHeader("Content-Length");
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 本クラスをインスタンス化する際にはボディを表す{@link File}オブジェクトの指定が必須なため、
     * 本メソッドは必ず{@code false}を返す。
     *
     * @return 必ず{@link false}を返す
     */
    @Override
    @Published(tag = "architect")
    public boolean isBodyEmpty() {
        return false;
    }

    @Override
    public String getBodyString() {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    @Published(tag = "architect")
    public InputStream getBodyStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public HttpResponse cleanup() {
        if (deleteOnCleanup) {
            file.delete();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 本クラスではコンテンツパスを設定できないため、
     * 本メソッドは必ず{@code null}を返す。
     *
     * @return 必ず{@code null}を返す。
     */
    @Override
    public ResourceLocator getContentPath() {
        // 何もしない
        return null;
    }

    @Override
    public HttpResponse setBodyStream(InputStream bodyStream) {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public HttpResponse setContentPath(String path) {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public HttpResponse setContentPath(ResourceLocator resource) {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public HttpResponse write(CharSequence text) {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public HttpResponse write(byte[] bytes) {
        throw new UnsupportedOperationException("unsupported.");
    }

    @Override
    public HttpResponse write(ByteBuffer bytes) {
        throw new UnsupportedOperationException("unsupported.");
    }

}
