package br.com.granzoto.media_compressor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MimeSuperTypeExtractorTest {

    @Test
    void testExtractMimeSuperType_ValidMimeType() {
        // Arrange
        String mimeType = "video/mp4";

        // Act
        String result = MimeSuperTypeExtractor.extractMimeSuperType(mimeType);

        // Assert
        assertEquals("video", result, "The super type for 'video/mp4' should be 'video'.");
    }

    @Test
    void testExtractMimeSuperType_InvalidMimeTypeFormat() {
        // Arrange
        String mimeType = "invalidMimeType";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MimeSuperTypeExtractor.extractMimeSuperType(mimeType)
        );
        assertEquals("MimeType not supported. Try video/mp4 or image/png", exception.getMessage());
    }

    @Test
    void testExtractMimeSuperType_NullMimeType() {
        // Arrange
        String mimeType = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MimeSuperTypeExtractor.extractMimeSuperType(mimeType)
        );
        assertEquals("MimeType required", exception.getMessage());
    }

    @Test
    void testExtractMimeSuperType_EmptyMimeType() {
        // Arrange
        String mimeType = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MimeSuperTypeExtractor.extractMimeSuperType(mimeType)
        );
        assertEquals("MimeType required", exception.getMessage());
    }

    @Test
    void testExtractMimeSuperType_LeadingTrailingSpaces() {
        // Arrange
        String mimeType = " video/mp4 ";

        // Act
        String result = MimeSuperTypeExtractor.extractMimeSuperType(mimeType.trim());

        // Assert
        assertEquals("video", result, "The super type should handle leading and trailing spaces.");
    }
}
