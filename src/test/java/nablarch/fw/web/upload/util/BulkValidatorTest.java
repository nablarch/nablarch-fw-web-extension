package nablarch.fw.web.upload.util;

import nablarch.core.dataformat.DataRecord;
import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.FixedLengthDataRecordFormatter;
import nablarch.core.dataformat.InvalidDataFormatException;
import nablarch.test.support.db.helper.DatabaseTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author T.Kawasaki
 */
@RunWith(DatabaseTestRunner.class)
public class BulkValidatorTest extends TestSetUpper {

    /** IO例外が発生した場合、実行時例外にラップされること。 */
    @Test
    public void testIOException() {

        // IO例外を発生ささせるスタブ
        final DataRecordFormatter formatter = new FixedLengthDataRecordFormatter() {
            @Override
            public boolean hasNext() throws IOException {
                return true;
            }

            @Override
            public DataRecord readRecord() throws IOException, InvalidDataFormatException {
                throw new IOException("for testing.");
            }
        };

        // ダミーの引数
        BasicValidatingStrategy<Form> dummy
                = new BasicValidatingStrategy<Form>(
                Form.class,
                "dummy",
                new BulkValidator.ErrorHandlingBulkValidator(null, "dummy", "dummy", "dummy"));

        try {
            // IO例外が発生する
            new BulkValidator.BulkValidationDriver<Form>(dummy, formatter, "hoge.txt").validateAll();
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
            assertThat(e.getMessage(), containsString("for testing."));
        }
    }
}
