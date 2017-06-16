package nablarch.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * テスト用のファイルを提供するクラス
 *
 * Created by ryota yoshinouchi
 */
public class FileProvider {

    /** デフォルトコンストラクタ。 */
    public FileProvider() {
    }

    /**
     *
     * @param path ファイルのパス
     * @param contents ファイル内のテキスト
     * @return 引数をもとに生成されたファイルオブジェクト
     */
    public static File file(String path, String contents){

        assert path!=null && !(path.length() == 0);

        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Charset charset = Charset.defaultCharset();

        FileChannel out = null;

        try {
            out = new FileOutputStream(file, false).getChannel();
            out.write(charset.encode(contents));
        }
        catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            try { out.close(); } catch(Exception ex) {/* Nothing to do. */}
        }
        return file;
    }


}
