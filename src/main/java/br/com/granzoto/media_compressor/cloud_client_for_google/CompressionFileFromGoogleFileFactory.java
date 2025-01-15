package br.com.granzoto.media_compressor.cloud_client_for_google;

import br.com.granzoto.media_compressor.model.*;
import com.google.api.services.drive.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public class CompressionFileFromGoogleFileFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionFileFromGoogleFileFactory.class);

    public static CompressionFile createCompressionFile(File googleFile, Map<String, FolderInfo> folderPaths) throws IOException {
        if (!googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            LOGGER.warn("Unable to build Google file with 'size, 'parents' and 'mimeType' fields. File: {}", googleFile.getName());
        }
        var fileName = FileExtensionFixer.fixFileExtensionIfNull(googleFile.getName(), googleFile.getMimeType());
        var size = googleFile.containsKey("size") ? BigInteger.valueOf(Long.parseLong(googleFile.get("size").toString())) : BigInteger.ZERO;
        var parentFolderId = googleFile.getParents() != null ? googleFile.getParents().getFirst() : null;
        var folderPath = FolderPathResolver.resolveFolderPath(parentFolderId, folderPaths);
        var mimeSuperType = MimeSuperTypeExtractor.extractMimeSuperType(googleFile.getMimeType());
        var inputFile = FileFactory.createInputFile(googleFile, folderPath);
        var outputFile = FileFactory.createOutputFile(googleFile, folderPath);
        return new CompressionFile(googleFile.getId(), fileName, size, folderPath, googleFile.getMimeType(), mimeSuperType,
                inputFile, outputFile);
    }
}
