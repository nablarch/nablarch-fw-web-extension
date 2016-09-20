package nablarch.fw.web.upload.util;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.fw.web.upload.util.BulkValidator.ErrorHandlingBulkValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * 典型的なバリデーション処理を提供する、{@link ValidatingStrategy}の基本実装クラス。
 *
 * @param <FORM> バリデーションに使用するフォームクラスの型
 * @author T.Kawasaki
 */
public class BasicValidatingStrategy<FORM> implements ValidatingStrategy<FORM> {

    /** フォームクラス */
    private final Class<FORM> formClass;

    /** バリデーションメソッド名 */
    private final String validateFor;

    /** エラー発生時のメッセージIDを持つクラス */
    private ErrorHandlingBulkValidator msgIds;

    /**
     * コンストラクタ。
     *
     * @param formClass                  フォームクラス
     * @param validateFor                バリデーションメソッド名
     * @param msgIds 形式エラー時のメッセージID
     */
    public BasicValidatingStrategy(
            Class<FORM> formClass, String validateFor, ErrorHandlingBulkValidator msgIds) {

        this.validateFor = validateFor;
        this.formClass = formClass;
        this.msgIds = msgIds;
    }

    /**
     * {@inheritDoc}
     * 本実装では、コンストラクタで指定されたバリデーションメソッドを起動する。
     */
    public ValidationContext<FORM> validateRecord(DataRecord dataRecord) {
        return ValidationUtil.validateAndConvertRequest(
                formClass, dataRecord, validateFor);
    }

    /**
     * {@inheritDoc}
     * 本実装では、発生したバリデーションエラーメッセージを、
     * コンストラクタで指定されたメッセージで置き換える。
     */
    public List<Message> handleInvalidRecord(
            DataRecord errorRecord, ValidationContext<FORM> context) {

        List<Message> result = new ArrayList<Message>(context.getMessages().size());
        for (Message e : context.getMessages()) {
            Message edited = MessageUtil.createMessage(
                    MessageLevel.ERROR,
                    msgIds.msgIdOnValidationError,
                    errorRecord.getRecordNumber(),
                    e.formatMessage());
            result.add(edited);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 本実装では、発生した形式エラーを、
     * コンストラクタで指定されたメッセージで置き換える。
     */
    public Message handleInvalidFormatRecord(InvalidDataFormatException e) {
        return MessageUtil.createMessage(
                MessageLevel.ERROR,
                msgIds.msgIdOnFormatError,
                e.getRecordNumber());
    }

    /** {@inheritDoc} */
    public void handleEmptyFile(String fileName) {
        throw new ApplicationException(
                MessageUtil.createMessage(
                        MessageLevel.ERROR,
                        msgIds.msgIdOnEmptyFile,
                        fileName));
    }


}
