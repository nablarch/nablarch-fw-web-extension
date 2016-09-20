package nablarch.common.web.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.FormatterFactory;
import nablarch.core.util.FilePathSetting;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpResponse;

/**
 * Map型のデータレコードのリストを一定のフォーマットに従って直列化し、
 * その内容をレスポンスボディとするHTTPレスポンスオブジェクト。
 *
 * @author Kiyohito Itoh
 */
public class DataRecordResponse extends HttpResponse {

    /** フォーマットで使用する出力ストリーム */
    private final ByteArrayOutputStream dest = new ByteArrayOutputStream();
    
    /** データレコードのフォーマッタ */
    private final DataRecordFormatter formatter;

    /**
     * コンストラクタ。
     * <p/>
     * フォーマット定義ファイルを元に、使用する{@link DataRecordFormatter}を設定する。
     *
     * @param  basePathName フォーマット定義ファイルのベースパス論理名
     * @param  fileName フォーマット定義ファイルのファイル名
     */
    @Published
    public DataRecordResponse(String basePathName, String fileName) {
        File layoutFile = FilePathSetting.getInstance().getFileWithoutCreate(basePathName, fileName);
        formatter = FormatterFactory.getInstance().createFormatter(layoutFile);
        formatter.setOutputStream(dest).initialize();
    }

    /**
     * メッセージボディに1レコード分のデータを書き込む。
     * <p/>
     * データレイアウト(レコードタイプ）の決定方法については
     * {@link DataRecordFormatter#writeRecord(Map)}を参照すること。
     *
     * @param record 1レコード分のデータ
     * @throws RuntimeException 出力ストリームの書き込みに失敗した場合
     * @throws nablarch.core.dataformat.InvalidDataFormatException 書き込むデータの内容がフォーマット定義に違反している場合。
     * @see DataRecordFormatter#writeRecord(Map)
     */
    @Published
    public void write(Map<String, ?> record) {
        try {
            formatter.writeRecord(record);
            write(dest.toByteArray());
            dest.reset();
        } catch (IOException e) {
            throw new RuntimeException(
                String.format("an error occurred while writing a record. record = [%s]", record), e);
        }
    }

    /**
     * データレイアウト(レコードタイプ）を指定して、メッセージボディに1レコード分のデータを書き込む。
     *
     * @param recordType 出力時に使用するデータレイアウト
     * @param record 1レコード分のデータ
     * @throws RuntimeException 出力ストリームの書き込みに失敗した場合
     * @throws nablarch.core.dataformat.InvalidDataFormatException 書き込むデータの内容がフォーマット定義に違反している場合。
     * @see DataRecordFormatter#writeRecord(String, Map)
     */
    @Published
    public void write(String recordType, Map<String, ?> record) {
        try {
            formatter.writeRecord(recordType, record);
            write(dest.toByteArray());
            dest.reset();
        } catch (IOException e) {
            throw new RuntimeException(
                String.format("an error occurred while writing a record. "
                            + "recordType = [%s], record = [%s]", recordType, record), e);
        }
    }

    @Override
    public HttpResponse cleanup() {
        FileUtil.closeQuietly(dest);
        return super.cleanup();
    }
}
