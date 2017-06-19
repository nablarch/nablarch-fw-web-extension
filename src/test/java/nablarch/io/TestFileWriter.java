package nablarch.io;


import java.io.*;
import java.nio.charset.Charset;

/**
 * ファイル出力を行うクラス
 *
 * @author ryota yoshinouchi
 */
public class TestFileWriter {

    /** ファイルの保存先となるパス */
    private String directoryPath;

    /**
     * コンストラクタ
     * 指定された引数で初期化を行う
     */
    public TestFileWriter(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * ファイル出力を行う
     *
     * @param fileName ファイル名
     * @param contents ファイル内のテキスト
     * @return 引数をもとに生成されたファイルオブジェクト
     */
    public File writeFile(String fileName, String... contents) throws IOException {

        String createFilePath = directoryPath + "/" + fileName;

        File file = new File(createFilePath);

        Charset charset = Charset.defaultCharset();

        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            for(int i = 0; i < contents.length - 1; i++){
                bw.write(contents[i]);
                bw.newLine();
            }
            bw.write(contents[contents.length - 1]);
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
        return file;
    }


}
