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

        OutputStream os = null;
        Writer w = null;
        BufferedWriter bw = null;

        try {
            os = new FileOutputStream(file);
            w  = new OutputStreamWriter(os, charset);
            bw = new BufferedWriter(w);
            for(int i = 0; i < contents.length - 1; i++){
                bw.write(contents[i]);
                bw.newLine();
            }
            bw.write(contents[contents.length - 1]);
        } catch (IOException e) {
            throw e;
        } finally {
            if (bw != null) bw.close();
        }
        return file;
    }


}
