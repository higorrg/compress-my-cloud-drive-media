package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompressionFileFactoryTest {

    @Test
    void testCreateCompressionFileFromGoogleFile_ValidInput() {
        // Arrange: Create a mock File object
        File mockGoogleFile = mock(File.class);
        when(mockGoogleFile.get("size")).thenReturn("123456");
        when(mockGoogleFile.containsKey("size")).thenReturn(true);
        when(mockGoogleFile.containsKey("parents")).thenReturn(true);
        when(mockGoogleFile.containsKey("mimeType")).thenReturn(true);
        when(mockGoogleFile.getParents()).thenReturn(List.of("parent-folder-id"));
        when(mockGoogleFile.getMimeType()).thenReturn("video/mp4");
        when(mockGoogleFile.getId()).thenReturn("file-id");
        when(mockGoogleFile.getName()).thenReturn("example.mp4");

        // Act
        CompressionFile result = CompressionFileFactory.createCompressionFileFromGoogleFile(mockGoogleFile);

        // Assert
        assertNotNull(result);
        assertEquals("file-id", result.id());
        assertEquals("example.mp4", result.name());
        assertEquals(BigInteger.valueOf(123456), result.size());
        assertEquals("parent-folder-id", result.parent());
        assertEquals("video", result.mimeSuperType());
    }

    @Test
    void testCreateCompressionFileFromGoogleFile_MissingFields() {
        // Arrange: Mock File without required fields
        File mockGoogleFile = mock(File.class);
        when(mockGoogleFile.containsKey("size")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CompressionFileFactory.createCompressionFileFromGoogleFile(mockGoogleFile)
        );

        assertEquals("Google file must have 'size', 'parents' and 'mimeType' extra fields", exception.getMessage());
    }

    @Test
    void testCreateCompressionFileFromGoogleFile_InvalidSize() {
        // Arrange: Mock File with invalid size
        File mockGoogleFile = mock(File.class);
        when(mockGoogleFile.containsKey("size")).thenReturn(true);
        when(mockGoogleFile.containsKey("parents")).thenReturn(true);
        when(mockGoogleFile.containsKey("mimeType")).thenReturn(true);
        when(mockGoogleFile.get("size")).thenReturn("invalid-size");
        when(mockGoogleFile.getParents()).thenReturn(List.of("parent-folder-id"));
        when(mockGoogleFile.getMimeType()).thenReturn("video/mp4");

        // Act & Assert
        assertThrows(NumberFormatException.class, () ->
                CompressionFileFactory.createCompressionFileFromGoogleFile(mockGoogleFile)
        );
    }

    @Test
    void testExtractMimeSuperType_ValidMimeType() {
        // Act
        String mimeSuperType = CompressionFileFactory.extractMimeSuperType("image/png");

        // Assert
        assertEquals("image", mimeSuperType);
    }

    @Test
    void testExtractMimeSuperType_NullMimeType() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CompressionFileFactory.extractMimeSuperType(null)
        );
        assertEquals("MimeType required", exception.getMessage());
    }

    @Test
    void testExtractMimeSuperType_EmptyMimeType() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CompressionFileFactory.extractMimeSuperType("")
        );
        assertEquals("MimeType required", exception.getMessage());
    }

}
