package nablarch.common.web.download;

import nablarch.core.util.FilePathSetting;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * {@link DataRecordResponse}のテスト。
 * @author Kiyohito Itoh
 */
public class DataRecordResponseTest {

    /**
     * 不正なレイアウト定義ファイルのパスが指定された場合。
     */
    @Test
    public void testInvalidLayoutFilePath() {
        FilePathSetting.getInstance().addBasePathSetting("format", "file:.");
        try {
            new DataRecordResponse("format", "notFoundFile");
            fail("IllegalArgumentExceptionがスローされること。");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(allOf(
                    containsString("invalid layout file path was specified."),
                    containsString("file path=["),
                    containsString(new File("./notFoundFile").getAbsolutePath()))));
        }
    }
}
