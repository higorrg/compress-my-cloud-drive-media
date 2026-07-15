package br.com.granzoto.media_compressor.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileFactory {

    static final String ORIGINAL_PATH = "Original_Files";
    static final String COMPRESSED_PATH = "Compressed_Files";

    /**
     * {@code id} is included in the path (not just {@code folderPath} + {@code fileName}) because
     * Google Drive allows different files to share the same name within folders that resolve to
     * the same path, which would otherwise make two unrelated Drive files collide on the same
     * local file, causing one file's compressed content to be uploaded over another file.
     */
    public static File createInputFile(String id, String fileName, String folderPath) throws IOException {
        return Path.of(UserOptions.getInstance().getDownloadPath(),
                        ORIGINAL_PATH,
                        folderPath,
                        id,
                        fileName)
                .toFile();
    }

    public static File createOutputFile(String id, String fileName, String folderPath) throws IOException {
        return Path.of(UserOptions.getInstance().getDownloadPath(),
                        COMPRESSED_PATH,
                        folderPath,
                        id,
                        fileName)
                .toFile();
    }
}
