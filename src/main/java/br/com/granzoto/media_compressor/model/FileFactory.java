package br.com.granzoto.media_compressor.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileFactory {

    static final String ORIGINAL_PATH = "Original_Files";
    static final String COMPRESSED_PATH = "Compressed_Files";

    public static File createInputFile(com.google.api.services.drive.model.File googleFile, String folderPath) throws IOException {
        return Path.of(UserOptions.getInstance().getDownloadPath(),
                        ORIGINAL_PATH,
                        folderPath,
                        googleFile.getName())
                .toFile();
    }

    public static File createOutputFile(com.google.api.services.drive.model.File googleFile, String folderPath) throws IOException {
        return Path.of(UserOptions.getInstance().getDownloadPath(),
                        COMPRESSED_PATH,
                        folderPath,
                        googleFile.getName())
                .toFile();
    }
}
