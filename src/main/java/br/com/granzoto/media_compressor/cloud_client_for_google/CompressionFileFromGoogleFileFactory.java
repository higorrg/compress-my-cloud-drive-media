package br.com.granzoto.media_compressor.cloud_client_for_google;

import br.com.granzoto.media_compressor.model.*;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public class CompressionFileFromGoogleFileFactory {

    public static CompressionFile createCompressionFile(File googleFile, Map<String, FolderInfo> folderPaths) throws IOException {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            throw new IllegalArgumentException("Google file must have 'size', 'parents' and 'mimeType' fields");
        }
        var fileName = FileExtensionFixer.fixFileExtensionIfNull(googleFile.getName(), googleFile.getMimeType());
        var size = BigInteger.valueOf(Long.parseLong(googleFile.get("size").toString()));
        var parentFolderId = googleFile.getParents() != null ? googleFile.getParents().getFirst() : null;
        var folderPath = FolderPathResolver.resolveFolderPath(parentFolderId, folderPaths);
        var mimeSuperType = MimeSuperTypeExtractor.extractMimeSuperType(googleFile.getMimeType());
        var inputFile = FileFactory.createInputFile(googleFile, folderPath);
        var outputFile = FileFactory.createOutputFile(googleFile, folderPath);
        return new CompressionFile(googleFile.getId(), fileName, size, folderPath, googleFile.getMimeType(), mimeSuperType,
                inputFile, outputFile);
    }
}
