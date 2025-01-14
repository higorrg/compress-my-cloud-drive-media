package br.com.granzoto.media_compressor.model;

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
     *
     * @return The filename with extension
     **/
    public static String fixFileExtensionIfNull(String currentFileName, String mimeType) {
        if (Strings.isNullOrEmpty(FilenameUtils.getExtension(currentFileName))){
            return currentFileName+"."+ extractFileExtensionFromMimeType(mimeType);
        }
        return currentFileName;
    }

    static String extractFileExtensionFromMimeType(String mimeType){
        if (Strings.isNullOrEmpty(mimeType)){
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        return mimeTypeList.getLast().toLowerCase();
    }
}
