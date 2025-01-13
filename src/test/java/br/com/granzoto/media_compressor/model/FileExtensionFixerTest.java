package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileExtensionFixerTest {

    @Test
    void testFixFileExtentionIfNull_AddsExtensionWhenMp4IsMissing() {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example");
        googleFile.setMimeType("video/mp4");

        // Act
        FileExtensionFixer.fixFileExtensionIfNull(googleFile);

        // Assert
        assertEquals("example.mp4", googleFile.getName(), "The file extension should be added to the file name.");
    }

    @Test
    void testFixFileExtensionIfNull_LeavesNameUnchangedWhenMp4ExtensionExists() {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.mp4");
        googleFile.setMimeType("video/mp4");

        // Act
        FileExtensionFixer.fixFileExtensionIfNull(googleFile);

        // Assert
        assertEquals("example.mp4", googleFile.getName(), "The file name should remain unchanged.");
    }

    @Test
    void testFixFileExtensionIfNull_LeavesNameUnchangedWhenPdfExtensionExists() {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.pdf");
        googleFile.setMimeType("application/pdf");

        // Act
        FileExtensionFixer.fixFileExtensionIfNull(googleFile);

        // Assert
        assertEquals("example.pdf", googleFile.getName(), "The file name should remain unchanged.");
    }

    @Test
    void testExtractFileExtensionFromMimeType_ValidMimeType() {
        // Act
        String fileExtension = FileExtensionFixer.extractFileExtensionFromMimeType("video/mp4");

        // Assert
        assertEquals("mp4", fileExtension, "The file extension should be correctly extracted from the MIME type.");
    }

    @Test
    void testExtractFileExtensionFromMimeType_EmptyMimeType() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                FileExtensionFixer.extractFileExtensionFromMimeType("")
        );
    }
}
