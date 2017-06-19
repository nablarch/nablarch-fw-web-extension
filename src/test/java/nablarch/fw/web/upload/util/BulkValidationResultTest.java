package nablarch.fw.web.upload.util;

import nablarch.core.db.statement.ParameterizedSqlPStatement;
import nablarch.core.db.support.DbAccessSupport;
import nablarch.fw.web.upload.PartInfo;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link BulkValidationResult}のテストケース。
 *
 * @author T.Kawasaki
 */
@RunWith(DatabaseTestRunner.class)
public class BulkValidationResultTest extends TestSetUpper {

    @Before
    public void deleteTable() throws SQLException {
        VariousDbTestHelper.delete(TestCities.class);
    }

    @Test
    public void testEmptyFile() throws IOException, SQLException {
        BulkValidationResult<Form> target = new BulkValidationResult<Form>();
        // アップロードファイルを準備
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(File.createTempFile(getClass().getName(), "temp"));
        int cnt =  target.importAll(new MyStrategy());
        assertThat(cnt, is(0));
        assertRecordCount(0);
    }

    /**
     * ３件ごとにexecuteBatchする。
     * （総件数３件）
     *
     * @throws SQLException 予期しない例外
     */
    @Test
    public void testThree() throws SQLException {
        int cnt = exec(new BulkValidationResult<Form>(3));
        assertThat(cnt, is(3));
        assertRecordCount(3);
    }

    /**
     * ２件ごとにexecuteBatchする。
     * （総件数３件→１件あまるが最後にexecuteBatchされる）
     *
     * @throws SQLException 予期しない例外
     */
    @Test
    public void testTwo() throws SQLException {
        int cnt = exec(new BulkValidationResult<Form>(2));
        assertThat(cnt, is(3));
        assertRecordCount(3);
    }


    private void assertRecordCount(int expected) throws SQLException {
        assertThat(VariousDbTestHelper.findAll(TestCities.class).size(), is(expected));
    }

    private int exec(BulkValidationResult<Form> target) {

        target.addValidObject(new Form(1L, "tokyo"));
        target.addValidObject(new Form(2L, "osaka"));
        target.addValidObject(new Form(3L, "kyoto"));
        int cnt = target.importAll(new MyStrategy());
        TestSetUpper.tmConn.commit();
        return cnt;
    }

    private static class MyStrategy implements InsertionStrategy<Form> {
        private final DbAccessSupport dbAccessSupport = new DbAccessSupport(BasicValidatingStrategyTest.class);

        public ParameterizedSqlPStatement prepareStatement(Form form) {
            return dbAccessSupport.getParameterizedSqlStatement("INSERT_SQL", form);
        }

        public void addBatch(ParameterizedSqlPStatement statement, Form form) {
            statement.addBatchObject(form);
        }
    }
}
