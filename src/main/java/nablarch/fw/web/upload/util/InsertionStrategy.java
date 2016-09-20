package nablarch.fw.web.upload.util;

import nablarch.core.db.statement.ParameterizedSqlPStatement;
import nablarch.core.util.annotation.Published;

/**
 * 個別の登録ロジックを記述するためのインタフェース。
 * <p/>
 * 以下の処理を提供する。
 * <ul>
 * <li>プリペアドステートメントの作成</li>
 * <li>バリデーション済みオブジェクトのバッチ登録</li>
 * </ul>
 *
 * @param <FORM> 登録に使用するフォームクラスの型
 * @author T.Kawasaki
*/
@Published
public interface InsertionStrategy<FORM> {

    /**
     * プリペアドステートメントを作成する。
     *
     * @param form バリデーション済みオブジェクト
     * @return プリペアドステートメント
     */
    ParameterizedSqlPStatement prepareStatement(FORM form);

    /**
     * ステートメントにバッチ登録する。
     *
     * @param statement ステートメント
     * @param form      バリデーション済みオブジェクト
     */
    void addBatch(ParameterizedSqlPStatement statement, FORM form);
}
