package nablarch.fw.web.upload.util;

import static nablarch.core.util.Builder.concat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import nablarch.core.dataformat.DataRecordFormatter;
import nablarch.core.dataformat.FormatterFactory;
import nablarch.core.dataformat.SyntaxErrorException;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.BinaryUtil;
import nablarch.core.util.FilePathSetting;
import nablarch.core.util.annotation.Published;
import nablarch.fw.web.upload.PartInfo;

/**
 * アップロードファイルに対する定型処理を提供するユーティリティクラス。
 *
 * @author T.Kawasaki
 */
public class UploadHelper {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(UploadHelper.class);

    /** 処理対象のPart */
    private final PartInfo partInfo;

    /**
     * {@code UploadHelper}を生成する。
     *
     * @param partInfo 処理対象の{@link PartInfo}オブジェクト
     */
    @Published
    public UploadHelper(PartInfo partInfo) {
        this.partInfo = partInfo;
    }

    /**
     * アップロードされたファイルを移動する。
     *
     * @param basePathName {@link FilePathSetting}のベースパス論理名
     * @param fileName     移動後のファイル名
     */
    @Published
    public void moveFileTo(String basePathName, String fileName) {
        // 移動先ディレクトリを取得
        FilePathSetting setting = FilePathSetting.getInstance();
        File dir = setting.getBaseDirectory(basePathName);
        // 移動
        partInfo.moveTo(dir, fileName);
    }

    /**
     * フォーマットを適用する。
     * フォーマット定義ファイル取得先のディレクトリはデフォルト設定を使用する。
     *
     * @param layoutFileName フォーマット定義ファイル名
     * @return 一括バリデーションクラス
     * @throws IllegalStateException フォーマット適用に失敗した場合
     */
    @Published
    public BulkValidator applyFormat(String layoutFileName) {
        return applyFormat("format", layoutFileName);
    }

    /**
     * フォーマットを適用する。
     *
     * @param basePathName   {@link FilePathSetting}のベースパス論理名
     * @param layoutFileName フォーマット定義ファイル名
     * @return 一括バリデーションクラス
     * @throws IllegalStateException フォーマット適用に失敗した場合
     */
    @Published
    public BulkValidator applyFormat(String basePathName, String layoutFileName) {
        logContentOfUploaded();
        // 適用するフォーマット定義ファイルを取得
        File layoutFile = getLayoutFile(basePathName, layoutFileName);
        // フォーマッタに入力ストリームを設定（markSupportedでなければならないのでBufferedInputStreamを使用）
        InputStream in = new BufferedInputStream(partInfo.getInputStream());
        DataRecordFormatter formatter;
        try {
            formatter = FormatterFactory.getInstance()
                                        .createFormatter(layoutFile)
                                        .setInputStream(in)
                                        .initialize();
        } catch (SyntaxErrorException e) {
            throw createApplyFormatException(basePathName, layoutFileName, layoutFile, e);
        } catch (IllegalArgumentException e) {
            throw createApplyFormatException(basePathName, layoutFileName, layoutFile, e);
        }
        // フォーマットを適用した一括バリデーションクラスを返却する
        return new BulkValidator(formatter, partInfo.getFileName());
    }

    /**
     * applyFormat に失敗した際に送出する例外を作成する。
     * 
     * @param basePathName フォーマットファイルのベースパス名
     * @param layoutFileName フォーマットファイルの論理名
     * @param layoutFile フォーマットファイル
     * @param e 元例外
     * @return applyFormat に失敗した際に送出する例外
     */
    private IllegalStateException createApplyFormatException(String basePathName,
            String layoutFileName, File layoutFile, Throwable e) {
        return new IllegalStateException(concat(
                "fail applying format file. basePathName=[", basePathName, "] ",
                "layoutFileName=[", layoutFileName, "] ",
                "layoutFile=[", layoutFile.getAbsolutePath(), "]",
                "partInfo=[", partInfo, "]"), e);
    }

    /**
     * フォーマット定義ファイルを取得する。
     *
     * @param basePathName   FilePathSettingのベースパス論理名
     * @param layoutFileName フォーマット定義ファイル名
     * @return フォーマット定義ファイル
     */
    private File getLayoutFile(String basePathName, String layoutFileName) {
        File layoutFile = FilePathSetting.getInstance().getFileWithoutCreate(basePathName, layoutFileName);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug(concat(
                    "applying format file. basePathName=[", basePathName, "] ",
                    "layoutFileName=[", layoutFileName, "] ",
                    "layoutFile=[", layoutFile.getAbsolutePath(), "]"));
        }
        return layoutFile;
    }

    /**
     * ファイルをバイト配列に変換する。
     * </p>
     * 入力ストリームが必要な場合は、{@link nablarch.fw.web.upload.PartInfo#getInputStream()}を使用すること。
     *
     * @return バイト配列
     */
    @Published
    public byte[] toByteArray() {
        return BinaryUtil.toByteArray(partInfo.getInputStream());
    }


    /** アップロードファイルの中身をログ出力する。 */
    private void logContentOfUploaded() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug(concat(
                    "content of uploaded file is [",
                    BinaryUtil.convertToHexStringWithPrefix(toByteArray()),
                    "]"));
        }
    }

}

