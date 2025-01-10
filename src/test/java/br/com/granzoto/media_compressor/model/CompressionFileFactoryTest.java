package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompressionFileFactoryTest {

    private Map<String, FolderInfo> folderPaths;

    @BeforeEach
    void setUp() {
        folderPaths = new HashMap<>();
        folderPaths.put("folder1", new FolderInfo("FolderA", null));
        folderPaths.put("folder2", new FolderInfo("FolderB", null));
        folderPaths.put("folder3", new FolderInfo("SubFolderB1", "folder2"));
    }

    @Test
    void testCreateCompressionFileFromGoogleFile_ValidInput() {
        // Arrange
        File googleFile = mock(File.class);
        when(googleFile.containsKey("size")).thenReturn(true);
        when(googleFile.containsKey("parents")).thenReturn(true);
        when(googleFile.containsKey("mimeType")).thenReturn(true);
        when(googleFile.get("size")).thenReturn("123456");
        when(googleFile.getParents()).thenReturn(java.util.List.of("folder3"));
        when(googleFile.getId()).thenReturn("file1");
        when(googleFile.getName()).thenReturn("example.mp4");
        when(googleFile.getMimeType()).thenReturn("video/mp4");

        // Act
        CompressionFile compressionFile = CompressionFileFactory.createCompressionFileFromGoogleFile(googleFile, folderPaths);

        // Assert
        assertNotNull(compressionFile);
        assertEquals("file1", compressionFile.id());
        assertEquals("example.mp4", compressionFile.name());
        assertEquals(BigInteger.valueOf(123456), compressionFile.size());
        assertEquals("FolderB/SubFolderB1", compressionFile.folderPath());
        assertEquals("video", compressionFile.mimeSuperType());
    }

    @Test
    void testCreateCompressionFileFromGoogleFile_InvalidSize() {
        // Arrange
        File googleFile = mock(File.class);
        when(googleFile.containsKey("size")).thenReturn(true);
        when(googleFile.containsKey("parents")).thenReturn(true);
        when(googleFile.containsKey("mimeType")).thenReturn(true);
        when(googleFile.get("size")).thenReturn("invalid-size");
        when(googleFile.getParents()).thenReturn(java.util.List.of("folder3"));
        when(googleFile.getMimeType()).thenReturn("video/mp4");

        // Act & Assert
        assertThrows(NumberFormatException.class, () ->
                CompressionFileFactory.createCompressionFileFromGoogleFile(googleFile, folderPaths)
        );
    }

    @Test
    void testCreateCompressionFileFromGoogleFile_MissingFields() {
        // Arrange
        File googleFile = mock(File.class);
        when(googleFile.containsKey("size")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CompressionFileFactory.createCompressionFileFromGoogleFile(googleFile, folderPaths)
        );
        assertEquals("Google file must have 'size', 'parents' and 'mimeType' extra fields", exception.getMessage());
    }

    @Test
    void testExtractMimeSuperType_ValidMimeType() {
        // Act
        String mimeSuperType = CompressionFileFactory.extractMimeSuperType("image/png");

        // Assert
        assertEquals("image", mimeSuperType);
    }

    @Test
    void testExtractMimeSuperType_InvalidMimeType() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CompressionFileFactory.extractMimeSuperType("")
        );
        assertEquals("MimeType required", exception.getMessage());
    }

    @Test
    void testResolveFullPath_ValidPath() {
        // Act
        String fullPath = CompressionFileFactory.resolveFullPath("folder3", folderPaths);

        // Assert
        assertEquals("FolderB/SubFolderB1", fullPath);
    }

    @Test
    void testResolveFullPath_RootFolder() {
        // Act
        String fullPath = CompressionFileFactory.resolveFullPath(null, folderPaths);

        // Assert
        assertEquals("", fullPath);
    }
}
