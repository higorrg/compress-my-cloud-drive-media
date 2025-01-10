package br.com.granzoto.media_compressor.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.api.services.drive.model.File;
import com.google.common.base.Strings;

public class CompressionFileFactory {

    private static final String ROOT_FOLDER = "";

    public static CompressionFile createCompressionFileFromGoogleFile(File googleFile, Map<String, FolderInfo> folderPaths) {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            throw new IllegalArgumentException("Google file must have 'size', 'parents' and 'mimeType' extra fields");
        }
        BigInteger size = BigInteger.valueOf(Long.parseLong(googleFile.get("size").toString()));
        String parentFolderId = googleFile.getParents() != null ? googleFile.getParents().getFirst() : null;
        String folderPath = resolveFullPath(parentFolderId, folderPaths);
        return new CompressionFile(googleFile.getId(), googleFile.getName(), size, folderPath, extractMimeSuperType(googleFile.getMimeType()));
    }

    static String extractMimeSuperType(String mimeType){
        if (Strings.isNullOrEmpty(mimeType)){
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
}
