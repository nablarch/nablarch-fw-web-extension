package nablarch.fw.web.upload.util;

import java.util.List;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.message.Message;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ValidationContext;

/**
 * 個別のバリデーションロジックを記述するためのインタフェース。
 * 以下の処理を提供する。
 * <ul>
 * <li>１レコードに対するバリデーション処理</li>
 * <li>バリデーションエラーが発生した場合の処理</li>
 * <li>形式エラーが発生した場合の処理</li>
 * </ul>
 *
 * @param <FORM> バリデーションに使用するフォームクラスの型
 * @author T.Kawasaki
 */
@Published(tag = "architect")
public interface ValidatingStrategy<FORM> {

    /**
     * １件分のレコードをバリデーションする。
     *
     * @param dataRecord バリデーション対象となるレコード
     * @return バリデーション結果の {@link ValidationContext}
     */
    ValidationContext<FORM> validateRecord(DataRecord dataRecord);

    /**
     * バリデーションエラーレコードを処理する。
     *
     * @param errorRecord バリデーションエラーとなったレコード
     * @param context     バリデーション情報
     * @return エラーメッセージ
     */
    List<Message> handleInvalidRecord(DataRecord errorRecord, ValidationContext<FORM> context);

    /**
     * 形式エラーレコードを処理する。
     *
     * @param e 形式エラー
     * @return エラーメッセージ
     */
    Message handleInvalidFormatRecord(InvalidDataFormatException e);

    /**
     * 空ファイルの場合の処理を行う。
     *
     * @param fileName アップロードされたファイルのファイル名
     */
    void handleEmptyFile(String fileName);


}
