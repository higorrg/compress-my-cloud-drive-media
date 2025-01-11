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

    private static final String ROOT_FOLDER = "";
    private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Downloads",
            "Original_Files");
    private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Downloads",
            "Compressed_Files");

    public static CompressionFile createCompressionFileFromGoogleFile(File googleFile, Map<String, FolderInfo> folderPaths) throws IOException {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            throw new IllegalArgumentException("Google file must have 'size', 'parents' and 'mimeType' extra fields");
        }
        BigInteger size = BigInteger.valueOf(Long.parseLong(googleFile.get("size").toString()));
        String parentFolderId = googleFile.getParents() != null ? googleFile.getParents().getFirst() : null;
        String folderPath = resolveFullPath(parentFolderId, folderPaths);
        String mimeSuperType = extractMimeSuperType(googleFile.getMimeType());
        java.io.File inputFile = createInputFile(googleFile, mimeSuperType, folderPath);
        java.io.File outputFile = createOutputFile(googleFile, mimeSuperType, folderPath);
        return new CompressionFile(googleFile.getId(), googleFile.getName(), size, folderPath, mimeSuperType,
                inputFile, outputFile);
    }

    static String extractMimeSuperType(String mimeType) {
        if (Strings.isNullOrEmpty(mimeType)) {
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        return mimeTypeList.getFirst();
    }

    static String resolveFullPath(String folderId, Map<String, FolderInfo> folderPaths) {
        if (Strings.isNullOrEmpty(folderId) || !folderPaths.containsKey(folderId)) {
            return ROOT_FOLDER;
        }

        StringBuilder path = new StringBuilder(folderPaths.get(folderId).name());
        String parentId = folderPaths.get(folderId).parentId();

        while (parentId != null && folderPaths.containsKey(parentId)) {
            path.insert(0, folderPaths.get(parentId).name() + "/");
            parentId = folderPaths.get(parentId).parentId();
        }
        return path.toString();
    }

    static java.io.File createInputFile(File file, String mimeSuperType, String folderPath) throws IOException {
        var inputFile = Path.of(DOWNLOAD_PATH.toString(),
                        folderPath,
                        file.getName())
                .toFile();
        FileUtils.createParentDirectories(inputFile);
        return inputFile;
    }

    static java.io.File createOutputFile(File file, String mimeSuperType, String folderPath) throws IOException {
        java.io.File outputFile = Path.of(UPLOAD_PATH.toString(),
                        folderPath,
                        file.getName())
                .toFile();
        FileUtils.createParentDirectories(outputFile);
        return outputFile;
    }
}
