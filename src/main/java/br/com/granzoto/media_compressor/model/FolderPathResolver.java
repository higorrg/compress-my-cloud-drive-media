package br.com.granzoto.media_compressor.model;

import com.google.common.base.Strings;

import java.util.Map;

public class FolderPathResolver {
    private static final String ROOT_FOLDER = "";

    public static String resolveFolderPath(String folderId, Map<String, FolderInfo> folderPaths) {
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
