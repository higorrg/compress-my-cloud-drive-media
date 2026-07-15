package br.com.granzoto.media_compressor.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileFactoryTest {

    @Test
    void testCreateInputFile_CreatesCorrectPath() throws IOException {
        // Act
        java.io.File inputFile = FileFactory.createInputFile("file-id-1", "example.mp4", "videos");

        // Assert
        assertNotNull(inputFile, "Input file should not be null.");
        assertEquals(Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.ORIGINAL_PATH).resolve("videos/file-id-1/example.mp4").toString(), inputFile.getPath(),
                "The input file path should match the expected path.");
    }

    @Test
    void testCreateOutputFile_CreatesCorrectPath() throws IOException {
        // Act
        java.io.File outputFile = FileFactory.createOutputFile("file-id-1", "example.mp4", "videos");

        // Assert
        assertNotNull(outputFile, "Output file should not be null.");
        String outputFilePath = Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.COMPRESSED_PATH).resolve("videos/file-id-1/example.mp4").toString();
        assertEquals(outputFilePath, outputFile.getPath(),
                "The output file path should match the expected path.");
    }

    @Test
    void testCreateInputFile_CreatesCorrectNestedPath() throws IOException {
        // Act
        java.io.File inputFile = FileFactory.createInputFile("file-id-1", "example.mp4", "videos/nested/folder");

        // Assert
        assertNotNull(inputFile, "Input file should not be null.");
        String inputFilePath = Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.ORIGINAL_PATH).resolve("videos/nested/folder/file-id-1/example.mp4").toString();
        assertEquals(inputFilePath, inputFile.getPath(),
                "The input file path should match the expected nested path.");
    }

    @Test
    void testCreateOutputFile_CreatesCorrectNestedPath() throws IOException {
        // Act
        java.io.File outputFile = FileFactory.createOutputFile("file-id-1", "example.mp4", "videos/nested/folder");

        // Assert
        assertNotNull(outputFile, "Output file should not be null.");
        String outputFilePath = Path.of(UserOptions.getInstance().getDownloadPath(), FileFactory.COMPRESSED_PATH).resolve("videos/nested/folder/file-id-1/example.mp4").toString();
        assertEquals(outputFilePath, outputFile.getPath(),
                "The output file path should match the expected nested path.");
    }

    @Test
    void testCreateInputFile_DifferentIdsWithSameNameAndFolderDoNotCollide() throws IOException {
        // Act
        java.io.File fileA = FileFactory.createInputFile("id-a", "photo.jpg", "Camera");
        java.io.File fileB = FileFactory.createInputFile("id-b", "photo.jpg", "Camera");

        // Assert
        assertNotEquals(fileA.getPath(), fileB.getPath(),
                "Two different Drive files with the same name and folder must not resolve to the same local path.");
    }
}
