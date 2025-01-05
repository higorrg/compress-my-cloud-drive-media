package br.com.granzoto.higor;

import java.math.BigInteger;
import java.util.List;

import com.google.api.services.drive.model.File;

public class CompressMyFileFromGoogleFactory {

    public static CompressMyFile createFile(File googleFile) {
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
        return new CompressMyFile(googleFile.getId(), googleFile.getName(), size, parentId);
    }
}
