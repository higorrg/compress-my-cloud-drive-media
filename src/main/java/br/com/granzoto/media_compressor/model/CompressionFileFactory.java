package br.com.granzoto.media_compressor.model;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import com.google.api.services.drive.model.File;
import com.google.common.base.Strings;

public class CompressionFileFactory {

    public static CompressionFile createCompressionFileFromGoogleFile(File googleFile) {
        if (!googleFile.containsKey("size") || !googleFile.containsKey("parents") || !googleFile.containsKey("mimeType")) {
            throw new IllegalArgumentException("Google file must have 'size', 'parents' and 'mimeType' extra fields");
        }
        BigInteger size = BigInteger.valueOf(Long.parseLong(googleFile.get("size").toString()));
        String parentId = googleFile.getParents().getFirst();
        return new CompressionFile(googleFile.getId(), googleFile.getName(), size, parentId, extractMimeSuperType(googleFile.getMimeType()));
    }

    static String extractMimeSuperType(String mimeType){
        if (Strings.isNullOrEmpty(mimeType)){
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        return mimeTypeList.getFirst();
    }

}
