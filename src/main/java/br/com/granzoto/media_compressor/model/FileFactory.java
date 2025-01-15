package br.com.granzoto.media_compressor.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileFactory {

    private static final String ORIGINAL_PATH = "Original_Files";
    private static final String COMPRESSED_PATH = "Compressed_Files";

    public static File createInputFile(com.google.api.services.drive.model.File googleFile, String folderPath) throws IOException {
        var inputFile = Path.of(UserOptions.getInstance().getDownloadPath(),
                        ORIGINAL_PATH,
                        folderPath,
                        googleFile.getName())
                .toFile();
        FileUtils.createParentDirectories(inputFile);
        return inputFile;
    }

    public static File createOutputFile(com.google.api.services.drive.model.File googleFile, String folderPath) throws IOException {
        java.io.File outputFile = Path.of(UserOptions.getInstance().getDownloadPath(),
                        COMPRESSED_PATH,
                        folderPath,
                        googleFile.getName())
                .toFile();
        FileUtils.createParentDirectories(outputFile);
        return outputFile;
    }
}
