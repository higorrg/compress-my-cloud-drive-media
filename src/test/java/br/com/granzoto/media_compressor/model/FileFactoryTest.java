package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileFactoryTest {

    @Test
    void testCreateInputFile_CreatesCorrectPath() throws IOException {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.mp4");
        String folderPath = "videos";

        // Act
        java.io.File inputFile = FileFactory.createInputFile(googleFile, folderPath);

        // Assert
        assertNotNull(inputFile, "Input file should not be null.");
        assertEquals(Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.ORIGINAL_PATH).resolve("videos/example.mp4").toString(), inputFile.getPath(),
                "The input file path should match the expected path.");
    }

    @Test
    void testCreateOutputFile_CreatesCorrectPath() throws IOException {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.mp4");
        String folderPath = "videos";

        // Act
        java.io.File outputFile = FileFactory.createOutputFile(googleFile, folderPath);

        // Assert
        assertNotNull(outputFile, "Output file should not be null.");
        String outputFilePath = Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.COMPRESSED_PATH).resolve("videos/example.mp4").toString();
        assertEquals(outputFilePath, outputFile.getPath(),
                "The output file path should match the expected path.");
    }

    @Test
    void testCreateInputFile_CreatesParentDirectories() throws IOException {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.mp4");
        String folderPath = "videos/nested/folder";

        // Act
        java.io.File inputFile = FileFactory.createInputFile(googleFile, folderPath);

        // Assert
        assertNotNull(inputFile, "Input file should not be null.");
        assertTrue(inputFile.getParentFile().exists(), "Parent directories should exist.");
        String inputFilePath = Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.ORIGINAL_PATH).resolve("videos/nested/folder/example.mp4").toString();
        assertEquals(inputFilePath, inputFile.getPath(),
                "The input file path should match the expected nested path.");
    }

    @Test
    void testCreateOutputFile_CreatesParentDirectories() throws IOException {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.mp4");
        String folderPath = "videos/nested/folder";

        // Act
        java.io.File outputFile = FileFactory.createOutputFile(googleFile, folderPath);

        // Assert
        assertNotNull(outputFile, "Output file should not be null.");
        assertTrue(outputFile.getParentFile().exists(), "Parent directories should exist.");
        String outputFilePath = Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.COMPRESSED_PATH).resolve("videos/nested/folder/example.mp4").toString();
        assertEquals(outputFilePath, outputFile.getPath(),
                "The output file path should match the expected nested path.");
    }
}