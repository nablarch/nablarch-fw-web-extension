package nablarch.fw.web.upload.util;

import static nablarch.core.util.Builder.concat;

import java.io.IOException;
import java.util.List;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.Message;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ValidationContext;

/**
 * アップロードファイルを一括バリデーションするためのクラス。
 * <p/>
 * バリデーションエラーが存在した場合でも処理を継続し、全レコードのバリデーションを実行する。
 *
 * @author T.Kawasaki
 */
public class BulkValidator {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(BulkValidator.class);

    /** アップロードファイルのレコードフォーマッタ */
    private final DataRecordFormatter formatter;

    /** アップロードされたファイルのファイル名 */
    private final String fileName;

    /**
     * コンストラクタ。
     *
     * @param formatter アップロードファイルのレコードフォーマッタ
     * @param fileName  アップロードされたファイルのファイル名
     */
    BulkValidator(DataRecordFormatter formatter, String fileName) {
        this.formatter = formatter;
        this.fileName = fileName;
    }

    /**
     * レコードフォーマッタを取得する。<br/>
     * 特殊な要件のため、独自のアップロード処理をおこなわなければならない場合、
     * 本メソッドで取得した{@link DataRecordFormatter}を使用して、
     * 任意の処理を実行できる。
     *
     * @return レコードフォーマッタ
     */
    public DataRecordFormatter getFormatter() {
        return formatter;
    }

    /**
     * 一括バリデーション処理を行う。
     * <p/>
     * 引数で与えられたバリデーションロジックを使用して、全レコードのバリデーション処理を行う。
     *
     * @param validatingStrategy バリデーションロジック
     * @param <FORM>             バリデーションに使用するフォームクラスの型
     * @return バリデーション結果
     */
    @Published
    public <FORM> BulkValidationResult<FORM> validateAll(ValidatingStrategy<FORM> validatingStrategy) {
        return new BulkValidationDriver<FORM>(validatingStrategy, formatter, fileName).validateAll();
    }

    /**
     * エラー発生時のメッセージIDを指定し、一括バリデーションクラスのインスタンスを生成する。
     * <p/>
     * 本FWが提供しているバリデーションのみで要件を満たせる場合は、本コンストラクタで生成した一括バリデーションクラスを使う。
     * 要件を満たせない場合は、{@link #validateAll(ValidatingStrategy)}を使用する。
     *
     * @param messageIdOnFormatError     形式エラー（{@link InvalidDataFormatException}）発生時のメッセージID
     * @param messageIdOnValidationError バリデーションエラー発生時のメッセージID
     * @param messageIdOnEmptyFile       ファイルが空の場合のメッセージID
     * @return 一括バリデーションクラスのインスタンス
     */
    @Published
    public ErrorHandlingBulkValidator setUpMessageIdOnError(String messageIdOnFormatError,
                                                            String messageIdOnValidationError,
                                                            String messageIdOnEmptyFile) {
        return new ErrorHandlingBulkValidator(
                this,
                messageIdOnFormatError,
                messageIdOnValidationError,
                messageIdOnEmptyFile);
    }

    /** エラー発生時にメッセージIDを設定する一括バリデーションクラス。 */
    public static final class ErrorHandlingBulkValidator {

        /** 一括バリデーションクラス（実際にバリデーションを起動するクラス） */
        private final BulkValidator bulkValidator;


        /** 形式エラー（{@link InvalidDataFormatException}）発生時のメッセージID */
        final String msgIdOnFormatError;     // SUPPRESS CHECKSTYLE  finalかつimmutableでカプセル化が不要なため
        /** バリデーションエラー発生時のメッセージID */
        final String msgIdOnValidationError; // SUPPRESS CHECKSTYLE  finalかつimmutableでカプセル化が不要なため
        /** 空ファイルの場合のメッセージID */
        final String msgIdOnEmptyFile;       // SUPPRESS CHECKSTYLE  finalかつimmutableでカプセル化が不要なため

        /**
         * コンストラクタ。
         *
         * @param validator         一括バリデーションクラス
         * @param onFormatError     形式エラー（{@link InvalidDataFormatException}）発生時のメッセージID
         * @param onValidationError バリデーションエラー発生時のメッセージID
         * @param onEmptyFile       ファイルが空の場合のメッセージID
         */
        ErrorHandlingBulkValidator(BulkValidator validator, String onFormatError, String onValidationError, String onEmptyFile) {
            this.bulkValidator = validator;
            this.msgIdOnFormatError = onFormatError;
            this.msgIdOnValidationError = onValidationError;
            this.msgIdOnEmptyFile = onEmptyFile;
        }

        /**
         * 指定されたフォームクラスを用いて一括バリデーション処理を行う。
         *
         * @param formClass   バリデーションに使用するフォームクラス
         * @param validateFor バリデーションメソッド名
         * @param <F>         フォームクラスの型
         * @return 一括バリデーション結果クラス
         */
        @Published
        public <F> BulkValidationResult<F> validateWith(Class<F> formClass, String validateFor) {
            BasicValidatingStrategy<F> strategy
                    = new BasicValidatingStrategy<F>(formClass, validateFor, this);
            return bulkValidator.validateAll(strategy);
        }
    }

    /**
     * 一括バリデーション実行クラス。
     *
     * @param <FORM> バリデーションに使用するフォームクラス
     */
    static class BulkValidationDriver<FORM> {

        /** 一括バリデーション実行結果クラス */
        private final BulkValidationResult<FORM> result = new BulkValidationResult<FORM>();

        /** バリデーションロジック */
        private final ValidatingStrategy<FORM> strategy;

        /** アップロードファイルのレコードフォーマッタ */
        private final DataRecordFormatter formatter;

        /** アップロードされたファイルのファイル名 */
        private final String fileName;

        /**
         * コンストラクタ
         *
         * @param strategy  バリデーションロジック
         * @param formatter アップロードファイルのレコードフォーマッタ
         * @param fileName  アップロードされたファイルのファイル名
         */
        BulkValidationDriver(ValidatingStrategy<FORM> strategy, DataRecordFormatter formatter, String fileName) {
            this.strategy = strategy;
            this.formatter = formatter;
            this.fileName = fileName;
        }

        /**
         * 一括バリデーション処理を実行する。
         * バリデーションエラーが存在した場合でも処理を継続し、全レコードのバリデーションを実行する
         * （例外は発生しない）。
         *
         * @return 一括バリデーション処理結果
         */
        BulkValidationResult<FORM> validateAll() {

            try {
                validateAllRecord();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                FileUtil.closeQuietly(formatter);
            }

            if (result.isEmpty()) {
                strategy.handleEmptyFile(fileName);
            }
            return result;
        }

        /**
         * 全レコードをバリデーションする。
         *
         * @throws IOException 入出力例外
         */
        private void validateAllRecord() throws IOException {
            while (formatter.hasNext()) {
                DataRecord dataRecord = null;
                try {
                    dataRecord = formatter.readRecord();
                    ValidationContext<FORM> context = strategy.validateRecord(dataRecord);
                    logRecord(dataRecord, context);
                    if (context.isValid()) {
                        // バリデーション成功
                        result.addValidObject(context.createObject());
                    } else {
                        // バリデーションエラーあり
                        logValidationError(dataRecord, context);
                        List<Message> messages = strategy.handleInvalidRecord(dataRecord, context);
                        result.addErrors(dataRecord.getRecordNumber(), messages);
                    }
                } catch (InvalidDataFormatException e) {
                    logFormatError(e);
                    // 形式エラー
                    result.addError(e.getRecordNumber(),
                            strategy.handleInvalidFormatRecord(e));
                    // continue loop until eof.
                }
            }
        }

        /**
         * レコードをログ出力する。
         *
         * @param dataRecord レコード
         * @param context    バリデーションコンテキスト
         */
        private void logRecord(DataRecord dataRecord, ValidationContext<FORM> context) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.logDebug(concat(
                        "invoking validation .",
                        " line=[", dataRecord.getRecordNumber(), "]",
                        " class=[", context.getTargetClass().getName(), "]",
                        " validateFor=[", context.getValidateFor(), "]",
                        " record=", dataRecord));
            }
        }

        /**
         * バリデーションエラーをログ出力する。
         *
         * @param errorRecord エラーレコード
         * @param context     バリデーション結果
         */
        private void logValidationError(DataRecord errorRecord, ValidationContext<FORM> context) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.logDebug(concat(
                        "   validation error .",
                        " line=[", errorRecord.getRecordNumber(), "]",
                        " messages=", context.getMessages()));
            }
        }

        /**
         * 形式エラーをログ出力する。
         *
         * @param e 形式エラー例外
         */
        private void logFormatError(InvalidDataFormatException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.logDebug(concat(
                        "       format error . ",
                        "line=[", e.getRecordNumber(), "] ",
                        "fieldName=[", e.getFieldName(), "] ",
                        e.getMessage()));

            }
        }
    }
}
