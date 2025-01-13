package br.com.granzoto.media_compressor.model;

import com.google.api.services.drive.model.File;
import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * When the file has no extension, ffmpeg fails on probing the mime type.
 * So, we need to redefine the name of the file to avoid ffmpeg to fail.
 */
public class FileExtensionFixer {


    /**
     * {@code googleFile.getFileExtension()} is not reliable because it
     * didn't work with pdf files.
     * **/
    static void fixFileExtensionIfNull(File googleFile) {
        if (Strings.isNullOrEmpty(FilenameUtils.getExtension(googleFile.getName()))){
            googleFile.setName(googleFile.getName()+"."+ extractFileExtensionFromMimeType(googleFile.getMimeType()));
        }
    }

    static String extractFileExtensionFromMimeType(String mimeType){
        if (Strings.isNullOrEmpty(mimeType)){
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        return mimeTypeList.getLast().toLowerCase();
    }
}
