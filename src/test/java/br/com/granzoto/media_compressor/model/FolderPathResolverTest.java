package br.com.granzoto.media_compressor.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FolderPathResolverTest {

    @Test
    void testResolveFolderPath_ValidNestedPath() {
        // Arrange
        Map<String, FolderInfo> folderPaths = new HashMap<>();
        folderPaths.put("root", new FolderInfo("Root", null));
        folderPaths.put("folderA", new FolderInfo("FolderA", "root"));
        folderPaths.put("folderB", new FolderInfo("FolderB", "folderA"));
        folderPaths.put("folderC", new FolderInfo("FolderC", "folderB"));

        // Act
        String fullPath = FolderPathResolver.resolveFolderPath("folderC", folderPaths);

        // Assert
        assertEquals("Root/FolderA/FolderB/FolderC", fullPath);
    }

    @Test
    void testResolveFolderPath_SingleLevelPath() {
        // Arrange
        Map<String, FolderInfo> folderPaths = new HashMap<>();
        folderPaths.put("folderA", new FolderInfo("FolderA", null));

        // Act
        String fullPath = FolderPathResolver.resolveFolderPath("folderA", folderPaths);

        // Assert
        assertEquals("FolderA", fullPath);
    }

    @Test
    void testResolveFolderPath_EmptyFolderId() {
        // Arrange
        Map<String, FolderInfo> folderPaths = new HashMap<>();

        // Act
        String fullPath = FolderPathResolver.resolveFolderPath("", folderPaths);

        // Assert
        assertEquals("", fullPath, "Empty folder ID should return the root folder.");
    }

    @Test
    void testResolveFolderPath_NullFolderId() {
        // Arrange
        Map<String, FolderInfo> folderPaths = new HashMap<>();

        // Act
        String fullPath = FolderPathResolver.resolveFolderPath(null, folderPaths);

        // Assert
        assertEquals("", fullPath, "Null folder ID should return the root folder.");
    }

    @Test
    void testResolveFolderPath_InvalidFolderId() {
        // Arrange
        Map<String, FolderInfo> folderPaths = new HashMap<>();
        folderPaths.put("folderA", new FolderInfo("FolderA", null));

        // Act
        String fullPath = FolderPathResolver.resolveFolderPath("invalidId", folderPaths);

        // Assert
        assertEquals("", fullPath, "Invalid folder ID should return the root folder.");
    }

}
