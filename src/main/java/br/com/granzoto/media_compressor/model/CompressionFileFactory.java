package br.com.granzoto.media_compressor.model;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

import com.google.api.services.drive.model.File;

public class CompressionFileFactory {

    public static CompressionFile createCompressionFileFromGoogleFile(File googleFile) throws IOException {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            throw new IllegalArgumentException("Google file must have 'size', 'parents' and 'mimeType' extra fields");
        }
        BigInteger size = BigInteger.valueOf(Long.valueOf(googleFile.get("size").toString()));
        String parentId = googleFile.getParents().get(0);
        return new CompressionFile(googleFile.getId(), googleFile.getName(), size, parentId, extractMimeSuperType(googleFile.getMimeType()));
    }

    private static String extractMimeSuperType(String mimeType){
        if (Objects.isNull(mimeType)){
            throw new IllegalArgumentException("MimeType required");
        }
        String[] mimeTypeItems = mimeType.split("/");
        if (mimeType.length() == 0){
            throw new IllegalArgumentException("MimeType not supported. Try video/mp4 or image/png");
        }
        return mimeTypeItems[0];
    }

}
