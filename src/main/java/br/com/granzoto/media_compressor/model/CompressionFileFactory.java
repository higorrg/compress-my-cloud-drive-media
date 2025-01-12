package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class CompressionFileFactory {

    public static CompressionFile createCompressionFileFromGoogleFile(File googleFile, Map<String, FolderInfo> folderPaths) throws IOException {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            throw new IllegalArgumentException("Google file must have 'size', 'parents' and 'mimeType' extra fields");
        }
        FileExtensionFixer.fixFileExtensionIfNull(googleFile);
        BigInteger size = BigInteger.valueOf(Long.parseLong(googleFile.get("size").toString()));
        String parentFolderId = googleFile.getParents() != null ? googleFile.getParents().getFirst() : null;
        String folderPath = FolderPathResolver.resolveFolderPath(parentFolderId, folderPaths);
        String mimeSuperType = MimeSuperTypeExtractor.extractMimeSuperType(googleFile.getMimeType());
        java.io.File inputFile = FileFactory.createInputFile(googleFile, folderPath);
        java.io.File outputFile = FileFactory.createOutputFile(googleFile, folderPath);
        return new CompressionFile(googleFile.getId(), googleFile.getName(), size, folderPath, googleFile.getMimeType(), mimeSuperType,
                inputFile, outputFile);
    }
}
