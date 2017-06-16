package nablarch.fw.web.upload.util;

import nablarch.core.db.support.DbAccessSupport;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.util.FilePathSetting;
import nablarch.fw.web.upload.PartInfo;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import nablarch.util.FileProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * {@link BasicValidatingStrategy}のテストケース。
 *
 * @author T.Kawasaki
 */
@RunWith(DatabaseTestRunner.class)
public class BasicValidatingStrategyTest extends TestSetUpper {
	
    /**
     * 一括登録時、精査が成功した場合は、ファイルの全レコードが登録されること
     *
     * @throws SQLException 予期しない例外
     */
    @Test
    public void testSuccess() throws SQLException {

        // アップロードファイルを準備
        File uploaded = FileProvider.file(FilePathSetting.getInstance().getBaseDirectory(FORMAT_BASE_PATH_NAME) + "/moge.txt", "1tokyo    2osaka    ");
        /*
        1tokyo    2osaka    */
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);

        // 実行
        new UploadHelper(part).applyFormat(FORMAT_BASE_PATH_NAME, "FMT001")
                              .setUpMessageIdOnError("MSG00001", "MSG00099", "MSG00050")
                              .validateWith(Form.class, "upload")
                              .importWith(new DbAccessSupport(getClass()), "INSERT_SQL");
        tmConn.commit();

        // 結果確認
        List<TestCities> testCities = VariousDbTestHelper.findAll(TestCities.class, "id");
        assertThat(testCities.size(), is(2));
        // １件目
        assertThat(testCities.get(0).id, is(1L));
        assertThat(testCities.get(0).city, is("tokyo"));
        // ２件目
        assertThat(testCities.get(1).id, is(2L));
        assertThat(testCities.get(1).city, is("osaka"));
    }

    /** 一括登録が精査失敗した場合、例外が送出されること。 */
    @Test
    public void testFail() {

        // アップロードファイルを準備
        File uploaded = FileProvider.file(FilePathSetting.getInstance().getBaseDirectory(FORMAT_BASE_PATH_NAME) + "/moge.txt", "Ztokyo    2aa       ");
        /*
        Ztokyo    2aa       */
        final PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);

        // 実行
        ApplicationException actual = null;
        try {
            new UploadHelper(part).applyFormat(FORMAT_BASE_PATH_NAME, "FMT001")
                                  .setUpMessageIdOnError("MSG00098", "MSG00099", "MSG00100")
                                  .validateWith(Form.class, "upload")
                                  .importWith(new DbAccessSupport(getClass()), "INSERT_SQL");
            fail();
        } catch (ApplicationException e) {
            actual = e;
        }
        String actualMessage = actual.getMessage();
        assertThat(actualMessage, containsString("1行目の値が不正です。"));
        assertThat(actualMessage, containsString("2行目にエラーがあります"));
        assertThat(actualMessage, containsString("cityは3文字以上10文字以下で入力してください。"));


        List<Message> messages = actual.getMessages();
        assertThat(messages.size(), is(2));
        // エラー１件目
        Message first = messages.get(0);
        assertThat(first.getLevel(), is(MessageLevel.ERROR));
        assertThat(first.getMessageId(), is("MSG00098"));
        // エラー２件目
        Message second = messages.get(1);
        assertThat(second.getLevel(), is(MessageLevel.ERROR));
        assertThat(second.getMessageId(), is("MSG00099"));
    }

    /** 空ファイルが渡された場合、例外が発生すること */
    @Test
    public void testEmptyFile() {

        BulkValidator.ErrorHandlingBulkValidator errors
                = new BulkValidator.ErrorHandlingBulkValidator(null, "MSG00098", "MSG00099", "MSG00100");

        BasicValidatingStrategy<Form> target
                = new BasicValidatingStrategy<Form>(Form.class, "upload", errors);
        try {
            target.handleEmptyFile("empty.txt");
            fail();
        } catch (ApplicationException e) {
            assertThat(e.getMessages().size(), is(1));
            assertThat(e.getMessages().get(0).getMessageId(), is("MSG00100"));
        }
    }
}
