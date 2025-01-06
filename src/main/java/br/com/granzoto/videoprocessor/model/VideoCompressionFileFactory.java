package br.com.granzoto.videoprocessor.model;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import com.google.api.services.drive.model.File;

public class VideoCompressionFileFactory {

    public static VideoCompressionFile createVideoCompressionFileFromGoogleFile(File googleFile) {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents")) {
            throw new IllegalArgumentException("Google file must have size and parents extra fields");
        }
        BigInteger size = BigInteger.valueOf(Long.valueOf(googleFile.get("size").toString()));
        Object parentIdRaw = googleFile.get("parents");
        String parentId = null;
        if (parentIdRaw instanceof List parentIdArray &&
                parentIdArray.size() > 0 &&
                parentIdArray.get(0) instanceof String parentIdString) {
            parentId = parentIdString;
        }
        return new VideoCompressionFile(googleFile.getId(), googleFile.getName(), size, parentId);
    }

    public static File createGoogleFileFromVideoCompressionFile(VideoCompressionFile compressionFile) {
        com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
        googleFile
                .setId(compressionFile.id())
                .setName(compressionFile.name())
                .setParents(Collections.singletonList(compressionFile.parentId()));
        return googleFile;
    }
}
