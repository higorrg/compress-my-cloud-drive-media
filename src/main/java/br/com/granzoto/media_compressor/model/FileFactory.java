package br.com.granzoto.media_compressor.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileFactory {
    static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Downloads",
            "Original_Files");
    static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Downloads",
            "Compressed_Files");

    public static File createInputFile(com.google.api.services.drive.model.File googleFile, String folderPath) throws IOException {
        var inputFile = Path.of(DOWNLOAD_PATH.toString(),
                        folderPath,
                        googleFile.getName())
                .toFile();
        FileUtils.createParentDirectories(inputFile);
        return inputFile;
    }

    public static File createOutputFile(com.google.api.services.drive.model.File googleFile, String folderPath) throws IOException {
        java.io.File outputFile = Path.of(UPLOAD_PATH.toString(),
                        folderPath,
                        googleFile.getName())
                .toFile();
        FileUtils.createParentDirectories(outputFile);
        return outputFile;
    }
}
