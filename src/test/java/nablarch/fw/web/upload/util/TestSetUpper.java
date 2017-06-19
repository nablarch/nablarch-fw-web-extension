package nablarch.fw.web.upload.util;

import nablarch.core.ThreadContext;
import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.initialization.Initializable;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.FilePathSetting;
import nablarch.core.validation.ValidateFor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.Required;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import nablarch.io.TestFileWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * @author T.Kawasaki
 */
public class TestSetUpper {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("nablarch/fw/web/upload/util/upload.xml");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected TestFileWriter testFileWriter;

    protected static TransactionManagerConnection tmConn;

    protected static final String FORMAT_BASE_PATH_NAME = TestSetUpper.class.getPackage().getName();

    private static final String FORMAT_SUFFIX = "fmt";

    private static final String[][] MESSAGES = {
        { "MSG00001", "ja", "{0}の値が不正です。", "en", "{0} value is invalid." },
        { "MSG00011", "ja", "{0}は必ず入力してください。", "en", "{0} is required." },
        { "MSG00021", "ja", "{0}は{2}文字以下で入力してください。", "en", "{0} cannot be greater than {2} characters." },
        { "MSG00022", "ja", "{0}は{1}文字以上{2}文字以下で入力してください。", "en", "{0} is not in the range {1} through {2}." },
        { "MSG00023", "ja", "{0}は{1}文字で入力してください。", "en", "{0} cannot be length {1}." },
        { "MSG00031", "ja", "{0}は整数{1}桁で入力してください。", "en", "{0} length must be under {1}." },
        { "MSG00041", "ja", "{0}は整数{1}桁で入力してください。", "en", "{0} length must be under {1}." },
        { "MSG00042", "ja", "{0}は整数部{1}桁、少数部{2}桁で入力してください。", "en", "{0} must be {1}-digits and {1}-digits decimal integer part." },
        { "MSG00051", "ja", "{0}は{2}以下で入力してください。", "en", "{0} cannot be greater than {2}." },
        { "MSG00052", "ja", "{0}は{1}以上{2}以下で入力してください。", "en", "{0} is not in the range {1} through {2}." },
        { "MSG00053", "ja", "{0}は{1}以上{2}以下で入力してください。", "en", "{0} is not in the range {1} through {2}." },
        { "MSG00098", "ja", "{0}行目の値が不正です。", "en", "format error found in line {0}." },
        { "MSG00099", "ja", "{0}行目にエラーがあります。[ {1} ]", "en", "invalid value found in line {0}. [ {1} ]" },
        { "MSG00100", "ja", "ファイルが空です。 ファイル=[{0}]", "en", "empty file uploaded. file=[{0}]" }
    };

    @BeforeClass
    public static void setupClass() {
    	VariousDbTestHelper.createTable(TestCities.class);
    }

    @Before
    public void setUp() throws Exception {

        ConnectionFactory factory = SystemRepository.get("connectionFactory");
        tmConn = factory.getConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY);
        DbConnectionContext.setConnection(tmConn);

        init("validationManager.formDefinitionCache",
                "validationManager",
                "statementValueObjectCache");

        repositoryResource.getComponentByType(MockStringResourceHolder.class).setMessages(MESSAGES);

        testFileWriter = new TestFileWriter(getDirectoryRootName());

		FilePathSetting.getInstance()
				.addBasePathSetting(FORMAT_BASE_PATH_NAME, "file:" + getDirectoryRootName())
				.addBasePathSetting("format", "file:" + getDirectoryRootName())
				.addFileExtensions(FORMAT_BASE_PATH_NAME, FORMAT_SUFFIX);
        createFormat();
        createInvalidFormat();
        ThreadContext.setLanguage(Locale.JAPAN);
        Locale.setDefault(Locale.JAPANESE);
    }

    @After
    public void tearDown() throws Exception {
        DbConnectionContext.removeConnection();
    	tmConn.terminate();
        DbConnectionContext.removeConnection();
    }

    File createFormat() throws IOException {
        String[] contents = {
                "file-type:     \"Fixed\"",
                "text-encoding: \"ms932\"",
                "",
                "# 各レコードの長さ",
                "record-length: 10",
                "",
                "# データレコード定義",
                "[Default]",
                "1    id            Z(1)",
                "2    city          X(9)",
                ""
        };
        return testFileWriter.writeFile("FMT001." + FORMAT_SUFFIX, contents);
    }

    File createInvalidFormat() throws IOException {
        return testFileWriter.writeFile("INVALID." + FORMAT_SUFFIX, "おかしなフォーマット定義ファイル");
    }

    protected String getDirectoryRootName() {
        return tempFolder.getRoot().toString();
    }

    private static void init(String... targetNames) {
        for (String e : targetNames) {
            Initializable initializable = SystemRepository.get(e);
            if (initializable != null) {
                initializable.initialize();
            }
        }
    }

    public static class Form {

        private Long id;

        private String city;

        public Form(Long id, String city) {
            this.id = id;
            this.city = city;
        }

        public Form(Map<String, Object> map) {
            this((Long) map.get("id"),
                 (String) map.get("city"));
        }

        public Long getId() {
            return id;
        }

        @Required
        @Digits(integer = 10, fraction = 0)
        public void setId(Long id) {
            this.id = id;
        }

        public String getCity() {
            return city;
        }

        @Required
        @Length(min = 3, max = 10)
        public void setCity(String city) {
            this.city = city;
        }

        @ValidateFor("upload")
        public static void validateFor(ValidationContext<Form> context) {
            ValidationUtil.validate(context, new String[]{"id", "city"});
        }
    }
}
