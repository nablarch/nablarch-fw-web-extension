package nablarch.common.web.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.ResponseBody;

/**
 * ストリームからHTTPレスポンスメッセージを生成するクラス。
 * <p/>
 * 本クラスは、ファイルシステム上のファイルやデータベースのBLOB型のカラムに格納した
 * バイナリデータのダウンロードに使用する。
 *
 * @author Kiyohito Itoh
 */
public class StreamResponse extends HttpResponse {

    /**
     * {@code StreamResponse}オブジェクトを生成する。
     *
     * @param blob バイナリラージオブジェクト
     * @throws RuntimeException ストリームアクセス時にエラーが発生した場合
     */
    @Published
    public StreamResponse(Blob blob) {
        try {
            initialize(blob.getBinaryStream());
        } catch (SQLException e) {
            throw new RuntimeException("an error occurred while initializing a response.", e);
        }
    }

    /**
     * 入力ストリームから初期化を行う。
     * 初期化完了後に入力ストリームを閉じる。
     * @param inputStream 入力ストリーム
     */
    private void initialize(InputStream inputStream) {
        int length;
        byte[] src = new byte[512];
        try {
            while ((length = inputStream.read(src)) != -1) {
                byte[] dest = new byte[length];
                System.arraycopy(src, 0, dest, 0, length);
                write(dest);
            }
        } catch (IOException e) {
            throw new RuntimeException("an error occurred while writing a response.", e);
        } finally {
            FileUtil.closeQuietly(inputStream);
        }
    }

    /**
     * {@code StreamResponse}オブジェクトを生成する。
     *
     * @param file ファイル
     * @param deleteOnCleanup リクエスト処理の終了時にダウンロード元のファイルを削除する場合は{@code true}
     */
    @Published
    public StreamResponse(File file, boolean deleteOnCleanup) {
        if (deleteOnCleanup) {
            ResponseBody.addTempFileToDelete(file);
        }
        setContentPath("file://" + file.getAbsolutePath());
    }
}
