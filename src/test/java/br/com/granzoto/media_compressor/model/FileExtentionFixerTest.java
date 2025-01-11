package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileExtentionFixerTest {

    @Test
    void testFixFileExtentionIfNull_AddsExtentionWhenMissing() {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example");
        googleFile.setMimeType("video/mp4");

        // Act
        FileExtentionFixer.fixFileExtentionIfNull(googleFile);

        // Assert
        assertEquals("example.mp4", googleFile.getName(), "The file extension should be added to the file name.");
    }

    @Test
    void testFixFileExtentionIfNull_LeavesNameUnchangedWhenExtensionExists() {
        // Arrange
        File googleFile = new File();
        googleFile.setName("example.mp4");
        googleFile.setMimeType("video/mp4");
        googleFile.setFileExtension("mp4");

        // Act
        FileExtentionFixer.fixFileExtentionIfNull(googleFile);

        // Assert
        assertEquals("example.mp4", googleFile.getName(), "The file name should remain unchanged.");
    }

    @Test
    void testExtractFileExtentionFromMimeType_ValidMimeType() {
        // Act
        String fileExtension = FileExtentionFixer.extractFileExtentionFromMimeType("video/mp4");

        // Assert
        assertEquals("mp4", fileExtension, "The file extension should be correctly extracted from the MIME type.");
    }

    @Test
    void testExtractFileExtentionFromMimeType_EmptyMimeType() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                FileExtentionFixer.extractFileExtentionFromMimeType("")
        );
    }
}
