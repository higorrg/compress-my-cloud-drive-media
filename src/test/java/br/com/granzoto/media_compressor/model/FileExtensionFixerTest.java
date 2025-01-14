package br.com.granzoto.media_compressor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileExtensionFixerTest {

    @Test
    void testFixFileExtensionIfNull_AddsExtensionWhenMp4IsMissing() {
        // Arrange
        var currentFileName ="example";
        var mimeType = "video/mp4";

        // Act
        var fixedFileName = FileExtensionFixer.fixFileExtensionIfNull(currentFileName, mimeType);

        // Assert
        assertEquals("example.mp4", fixedFileName, "The file extension should be added to the file name.");
    }

    @Test
    void testFixFileExtensionIfNull_LeavesNameUnchangedWhenMp4ExtensionExists() {
        // Arrange
        var currentFileName ="example.mp4";
        var mimeType = "video/mp4";

        // Act
        var fixedFileName = FileExtensionFixer.fixFileExtensionIfNull(currentFileName, mimeType);

        // Assert
        assertEquals("example.mp4", fixedFileName, "The file name should remain unchanged.");
    }

    @Test
    void testFixFileExtensionIfNull_LeavesNameUnchangedWhenPdfExtensionExists() {
        // Arrange
        var currentFileName ="example.pdf";
        var mimeType = "application/pdf";

        // Act
        var fixedFileName = FileExtensionFixer.fixFileExtensionIfNull(currentFileName, mimeType);

        // Assert
        assertEquals("example.pdf", fixedFileName, "The file name should remain unchanged.");
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
