package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;

public class FileExtentionFixer {

    static void fixFileExtentionIfNull(File googleFile) {
        if (Strings.isNullOrEmpty(googleFile.getFileExtension())){
            googleFile.setName(googleFile.getName()+"."+extractFileExtentionFromMimeType(googleFile.getMimeType()));
        }
    }

    static String extractFileExtentionFromMimeType(String mimeType){
        if (Strings.isNullOrEmpty(mimeType)){
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        return mimeTypeList.getLast().toLowerCase();
    }
}
