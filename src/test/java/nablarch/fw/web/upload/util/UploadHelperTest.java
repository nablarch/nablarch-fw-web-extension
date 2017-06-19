package nablarch.fw.web.upload.util;

import nablarch.core.ThreadContext;
import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.core.dataformat.SyntaxErrorException;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;
import nablarch.core.util.FilePathSetting;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.fw.web.upload.PartInfo;
import nablarch.fw.web.upload.util.BulkValidationResult.ErrorMessages;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link UploadHelper}のテストクラス。
 *
 * @author T.Kawasaki
 */
@RunWith(DatabaseTestRunner.class)
public class UploadHelperTest extends TestSetUpper {

    /**
     * アップロードファイルの移動ができること。
     *
     * @throws IOException
     */
    @Test
    public void testMoveUploadedFile() throws IOException {
        // 移動先の設定
        FilePathSetting.getInstance().addBasePathSetting("temp.dir", "file:" + tempFolder.getRoot());

        // パート情報を生成
        PartInfo part = PartInfo.newInstance("file");
        File f = File.createTempFile(getClass().getName(), ".tmp");
        part.setSavedFile(f);
        // テスト実行
        UploadHelper target = new UploadHelper(part);
        // アップロードファイルを移動
        target.moveFileTo("temp.dir", "hoge.txt");

        // ファイルが移動されていること
        File removed = new File(tempFolder.getRoot() + "/hoge.txt");
        assertThat(removed.exists(), is(true));
        removed.delete();
    }

    /**
     * アップロードファイルをバイト列に変換できること。
     *
     * @throws IOException
     */
    @Test
    public void testToByteArray() throws IOException {
        // アップロードファイルを準備
        String tempFile =  FilePathSetting.getInstance().getBaseDirectory(FORMAT_BASE_PATH_NAME) + "/fuga.txt";
        testFileWriter.writeFile("fuga.txt", "fugafuga");

        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(new File(tempFile));
        // テスト実行
        UploadHelper target = new UploadHelper(part);
        // ファイルの内容がバイト配列で取得できること
        String actual = new String(target.toByteArray());
        assertThat(actual, is("fugafuga"));
    }

    /**
     * アップロードされたファイルが空の時、例外が発生すること。
     *
     * @throws IOException 予期しない例外
     */
    @Test
    public void testEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("test", "txt");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(emptyFile);
        UploadHelper target = new UploadHelper(part);
        try {
            target.applyFormat(FORMAT_BASE_PATH_NAME, "FMT001")
                  .validateAll(new SampleValidatingStrategy());
            fail();
        } catch (ApplicationException e) {
            List<Message> messages = e.getMessages();
            assertThat(messages.size(), is(1));
            assertThat(messages.get(0).getMessageId(), is("MSG00100"));
            ThreadContext.setLanguage(Locale.getDefault());
            assertThat(e.getMessage(), containsString("ファイルが空です。 ファイル=[" + emptyFile.getName() + "]"));
        }
    }

    /**
     * 指定したフォーマットでDataRecordFormatterの取得ができること。
     *
     * @throws IOException 予期しない例外
     */
    @Test
    public void testGetFormatter() throws IOException {

        // アップロードファイルを準備
        File uploaded = testFileWriter.writeFile("fuga.txt", "1tokyo    2osaka    ");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);
        // 実行
        UploadHelper target = new UploadHelper(part);
        DataRecordFormatter formatter
                = target.applyFormat(FORMAT_BASE_PATH_NAME,
                "FMT001").getFormatter();
        try {
            assertNotNull(formatter);
        } finally {
            if (formatter != null) {
                formatter.close();
            }
        }
    }

    /**
     * 精査が成功した場合は、変換後のオブジェクトが取得できること。
     *
     * @throws IOException 予期しない例外
     */
    @Test
    public void testValidObjects() throws IOException {

        // アップロードファイルを準備
        File uploaded = testFileWriter.writeFile("fuga.txt", "1tokyo    2osaka    ");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);
        // 実行
        UploadHelper target = new UploadHelper(part);
        BulkValidationResult<Form> result
                = target.applyFormat(FORMAT_BASE_PATH_NAME, "FMT001")
                        .validateAll(new SampleValidatingStrategy());

        assertThat(result.hasError(), is(false));
        assertThat(result.getErrorMessages().isEmpty(), is(true));

        // 精査が成功したオブジェクトを取得できること。
        List<Form> validObjects = result.getValidObjects();
        // １件目
        Form first = validObjects.get(0);
        assertThat(first.getId(), is(1L));
        assertThat(first.getCity(), is("tokyo"));
        // ２件目
        Form second = validObjects.get(1);
        assertThat(second.getId(), is(2L));
        assertThat(second.getCity(), is("osaka"));
    }

    /**
     * 精査が失敗した時、エラーメッセージが取得できること。
     *
     * @throws IOException 予期しない例外
     */
    @Test
    public void testGetErrorMessages() throws IOException {

        // アップロードファイルを準備
        File uploaded = testFileWriter.writeFile("fuga.txt", "1ab       Zosaka    ");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);

        // 実行
        UploadHelper target = new UploadHelper(part);
        BulkValidationResult<Form> result
                = target.applyFormat(FORMAT_BASE_PATH_NAME, "FMT001")
                        .validateAll(new SampleValidatingStrategy());

        // エラーメッセージが取得できること
        ErrorMessages errorMessages = result.getErrorMessages();
        assertThat(result.hasError(), is(true));
        assertThat(errorMessages.size(), is(2));

        // １件目
        {
            List<Message> firstRow = errorMessages.get(1);
            assertThat(firstRow.size(), is(1));
            Message message = firstRow.get(0);
            assertThat(message.getLevel(), is(MessageLevel.ERROR));
            assertThat(message.getMessageId(), is("MSG00022"));
        }
        // ２件目
        {
            List<Message> secondRow = errorMessages.get(2);
            assertThat(secondRow.size(), is(1));
            Message message = secondRow.get(0);
            assertThat(message.getLevel(), is(MessageLevel.ERROR));
            assertThat(message.getMessageId(), is("MSG00001"));
        }
    }

    /**
     * 精査済みオブジェクトを取得する際、精査エラーが１件でもある場合は、
     * 例外が発生すること。
     *
     * @throws IOException 予期しない例外
     */
    @Test
    public void testGetValidObjectsFail() throws IOException {
        // アップロードファイルを準備
        File uploaded = testFileWriter.writeFile("moge.txt", "Ztokyo    2osaka    ");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);
        // 実行
        UploadHelper target = new UploadHelper(part);
        final BulkValidationResult<Form> result
                = target.applyFormat(FORMAT_BASE_PATH_NAME, "FMT001")
                        .validateAll(new SampleValidatingStrategy());
        assertThat(result.hasError(), is(true));

        try {
            result.getValidObjects();
            fail();
        } catch (ApplicationException e) {
            List<Message> errorMessages = e.getMessages();
            assertThat(result.hasError(), is(true));
            assertThat(errorMessages.size(), is(1));
            {
                Message message = errorMessages.get(0);
                assertThat(message.getLevel(), is(MessageLevel.ERROR));
                assertThat(message.getMessageId(), is("MSG00001"));
            }
        }
    }

    @Test
    public void testInvalidFormatDefinitionFile() throws IOException {
        // アップロードファイルを準備
        File uploaded = testFileWriter.writeFile("moge.txt", "1tokyo    2osaka    ");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);
        // 実行
        UploadHelper target = new UploadHelper(part);
        try {
            target.applyFormat("INVALID");
            fail();
        } catch (IllegalStateException e) {
            Throwable cause = e.getCause();
            assertEquals(SyntaxErrorException.class, cause.getClass());
            return;
        }
    }


    /**
     * レイアウトファイルが存在しない場合。
     *
     * @throws IOException 予期しない例外
     */
    @Test
    public void testLayoutFileNotFound() throws IOException {
        // アップロードファイルを準備
        File uploaded = testFileWriter.writeFile("moge.txt", "1tokyo    2osaka    ");
        PartInfo part = PartInfo.newInstance("fuga");
        part.setSavedFile(uploaded);
        
        File layoutFile = FilePathSetting.getInstance().getFile("format", "NOTFOUND");
        // 存在しないはずだが、念のため削除
        layoutFile.delete();
        
        // 実行
        UploadHelper target = new UploadHelper(part);
        try {
            target.applyFormat("NOTFOUND");
            fail();
        } catch (IllegalStateException e) {
            // 論理・物理のファイル名が欲しい
            assertThat(e.getMessage(), containsString(layoutFile.getAbsolutePath()));
            assertThat(e.getMessage(), containsString("layoutFileName=[NOTFOUND]"));
            assertFalse(layoutFile.exists());
            return;
        }
    }

    /** テスト用の精査ロジック */
    private static class SampleValidatingStrategy implements ValidatingStrategy<Form> {
        public ValidationContext<Form> validateRecord(DataRecord dataRecord) {
            return ValidationUtil.validateAndConvertRequest(Form.class, dataRecord, "upload");
        }

        public List<Message> handleInvalidRecord(DataRecord errorRecord, ValidationContext<Form> context) {
            return context.getMessages();
        }

        public Message handleInvalidFormatRecord(InvalidDataFormatException e) {
            return MessageUtil.createMessage(
                    MessageLevel.ERROR, "MSG00001", e.getRecordNumber() + "番目のレコード");
        }

        public void handleEmptyFile(String fileName) {
            throw new ApplicationException(
                    MessageUtil.createMessage(MessageLevel.ERROR, "MSG00100", fileName));
        }
    }


}
