package br.com.granzoto.media_compressor.model;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class MimeSuperTypeExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MimeSuperTypeExtractor.class);

    public static String extractMimeSuperType(String mimeType) {
        if (Strings.isNullOrEmpty(mimeType)) {
            LOGGER.warn("Unable to extract MimeSuperType");
            return null;
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        if (mimeTypeList.size() < 2) {
            LOGGER.warn("Unable to extract MimeSuperType from {}", mimeType);
        }
        return mimeTypeList.getFirst();
    }
}
