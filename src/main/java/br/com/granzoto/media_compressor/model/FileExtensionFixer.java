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

    /**
     * Google Drive's {@code name} field is just a display label: it can carry an extension that
     * doesn't match the file's real {@code mimeType} (e.g. two unrelated files sharing a name, or
     * a name backfilled from a missing extension elsewhere). ffmpeg picks its output container
     * purely from the output filename's extension, so local (on-disk) paths must always carry the
     * extension that matches the actual {@code mimeType}, regardless of what {@code currentFileName}
     * says, to avoid silently muxing content into the wrong container.
     *
     * @return {@code currentFileName} with its extension replaced by one derived from {@code mimeType}
     **/
    public static String forceExtensionFromMimeType(String currentFileName, String mimeType) {
        return FilenameUtils.removeExtension(currentFileName) + "." + extractFileExtensionFromMimeType(mimeType);
    }

    static String extractFileExtensionFromMimeType(String mimeType){
        if (Strings.isNullOrEmpty(mimeType)){
            throw new IllegalArgumentException("MimeType required");
        }
        var mimeTypeList = new ArrayList<String>(Arrays.asList(mimeType.split("/")));
        return mimeTypeList.getLast().toLowerCase();
    }
}
