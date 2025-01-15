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
    void testExtractMimeSuperType_LeadingTrailingSpaces() {
        // Arrange
        String mimeType = " video/mp4 ";

        // Act
        String result = MimeSuperTypeExtractor.extractMimeSuperType(mimeType.trim());

        // Assert
        assertEquals("video", result, "The super type should handle leading and trailing spaces.");
    }
}
