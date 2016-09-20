package nablarch.fw.web.upload.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import nablarch.core.db.statement.ParameterizedSqlPStatement;
import nablarch.core.db.support.DbAccessSupport;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.util.annotation.Published;

/**
 * 一括バリデーション結果を保持するクラス。
 * <p/>
 * バリデーション結果の取得やバリデーション済みオブジェクトの登録機能を持つ。
 *
 * @param <FORM> バリデーションに使用するフォームクラスの型
 * @author T.Kawasaki
 */
public class BulkValidationResult<FORM> {

    /** INSERT時の一括実行数のデフォルト値 */
    private static final int BATCH_SIZE = 100;

    /** バリデーション済みオブジェクト */
    private final List<FORM> validObjects = new ArrayList<FORM>();

    /** エラーメッセージ */
    private ErrorMessages errorMessages = new ErrorMessages();

    /** INSERT時の一括実行数 */
    private final int batchSize;

    /**
     * {@code BulkValidationResult}を生成する。
     */
    BulkValidationResult() {
        this(BATCH_SIZE);
    }

    /**
     * INSERT時の一括実行数を指定して、{@code BulkValidationResult}を生成する。
     *
     * @param batchSize 一括実行数
     */
    BulkValidationResult(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * エラーが発生しているかどうかを判定する。
     *
     * @return エラーが１件でも発生している場合は、{@code true}
     */
    @Published
    public boolean hasError() {
        return !errorMessages.isEmpty();
    }

    /**
     * エラーメッセージを取得する。
     * <p/>
     * エラーが発生していない場合、空の{@link ErrorMessages}が返却される。
     *
     * @return エラーメッセージ
     */
    @Published
    public ErrorMessages getErrorMessages() {
        return errorMessages;
    }

    /**
     * バリデーション対象があるかどうか。
     * <p/>
     * バリデーション実行前に呼び出した場合結果は保証されない。
     *
     * @return バリデーション対象がない場合{@code true}
     */
    public boolean isEmpty() {
        return validObjects.isEmpty() && errorMessages.isEmpty();
    }

    /**
     * バリデーション済みオブジェクトを取得する。
     *
     * @return バリデーション済みオブジェクト
     * @throws ApplicationException 一件でもバリデーションエラーが発生していた場合。
     *          この例外には、発生したすべてのバリデーションエラーのメッセージが格納されている。
     */
    public List<FORM> getValidObjects() throws ApplicationException {
        if (hasError()) {
            List<Message> all = errorMessages.getAllMessages();
            throw new ApplicationException(all);
        }
        return validObjects;
    }


    /**
     * 指定されたSQLIDを用いて、バリデーション済みオブジェクト({@link #getValidObjects}の結果)を一括登録する。
     *
     * @param dbAccessSupport 登録に使用する{@link DbAccessSupport}クラス
     * @param insertSqlId     登録に使用するSQLID
     * @return レコード登録件数(バリデーション済みオブジェクトがない場合は0を返す)
     */
    @Published
    public int importWith(final DbAccessSupport dbAccessSupport, final String insertSqlId) {
        InsertionStrategy<FORM> strategy = new InsertionStrategy<FORM>() {
            public ParameterizedSqlPStatement prepareStatement(FORM form) {
                return dbAccessSupport.getParameterizedSqlStatement(insertSqlId, form);
            }

            public void addBatch(ParameterizedSqlPStatement statement, FORM form) {
                statement.addBatchObject(form);
            }
        };
        return importAll(strategy);
    }


    /**
     * 登録ロジックを用いて、バリデーション済みオブジェクト({@link #getValidObjects}の結果)を一括登録する。
     *
     * @param strategy 登録ロジック
     * @return レコード登録件数(バリデーション済みオブジェクトがない場合は0を返す)
     */
    @Published
    public int importAll(InsertionStrategy<FORM> strategy) {
        List<FORM> validObjects = getValidObjects();
        if (validObjects.isEmpty()) {
            return 0;    // 空ファイルの場合
        }

        ParameterizedSqlPStatement statement = strategy.prepareStatement(validObjects.get(0));
        int cnt = 0;
        for (FORM e : validObjects) {
            strategy.addBatch(statement, e);
            cnt++;
            // 一定間隔ごとにexecuteBatch
            boolean timeToExec = isTimeToExecBatch(cnt);
            if (timeToExec) {
                statement.executeBatch();
            }
        }
        // 最後に余ったものをexecuteBatch
        if (!isTimeToExecBatch(cnt)) {
            statement.executeBatch();
        }
        return cnt;
    }

    /**
     * バッチ実行すべきタイミングであるかどうか判定する。
     *
     * @param cnt 現在のカウント
     * @return バッチ実行すべき場合は、{@code true}
     */
    private boolean isTimeToExecBatch(int cnt) {
        return cnt % batchSize == 0;
    }

    /**
     * バリデーション結果として、バリデーション済みのオブジェクトを追加する。
     *
     * @param validObject 追加するバリデーション済みオブジェクト
     */
    void addValidObject(FORM validObject) {
        validObjects.add(validObject);
    }

    /**
     * バリデーション結果として、エラーメッセージを追加する。
     *
     * @param recordNumber エラーとなったレコード行
     * @param messages     エラーメッセージ
     */
    void addErrors(int recordNumber, List<Message> messages) {
        errorMessages.put(recordNumber, messages);
    }

    /**
     * バリデーション結果として、エラーメッセージを追加する。
     *
     * @param recordNumber エラーとなったレコード行
     * @param message      エラーメッセージ
     */
    void addError(Integer recordNumber, Message message) {
        errorMessages.put(recordNumber, Arrays.asList(message));
    }

    /**
     * エラーメッセージ一覧を、行数の昇順で保持するクラス。
     * <pre>
     * キー：エラーが発生した行番号
     * 値  ：発生したエラーメッセージ
     * </pre>
     *
     * @author T.Kawasaki
     */
    public static class ErrorMessages extends TreeMap<Integer, List<Message>> {

        /**
         * 行数の昇順でソートされた、全てのエラーメッセージを取得する。
         *
         * @return 全てのエラーメッセージ
         */
        @Published
        public List<Message> getAllMessages() {
            return flatten(values());
        }

        /**
         * コレクションを要素とするコレクションを平坦化したリストとして返却する。
         *
         * @param orig 元のコレクション
         * @param <V>  要素の型
         * @return 平坦化されたリスト
         */
        private static <V> List<V> flatten(Collection<? extends Collection<V>> orig) {
            List<V> result = new ArrayList<V>();
            for (Collection<V> e : orig) {
                result.addAll(e);
            }
            return result;
        }
    }
}

