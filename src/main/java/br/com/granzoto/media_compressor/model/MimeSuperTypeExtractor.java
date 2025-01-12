package br.com.granzoto.media_compressor.model;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;

public class MimeSuperTypeExtractor {
    public static String extractMimeSuperType(String mimeType) {
        if (Strings.isNullOrEmpty(mimeType)) {
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        if (mimeTypeList.size() < 2){
            throw new IllegalArgumentException("MimeType not supported. Try video/mp4 or image/png");
        }
        return mimeTypeList.getFirst();
    }
}
