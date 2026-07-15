package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

/**
 * Represents the cloud drive that the user wants to compress his media files.
 */
public interface CloudClient {

    void addHandler(CloudClientHandler handler);
    void addStartObserver(CloudClientStartObserver observer);
    void addItemObserver(CloudClientItemObserver observer);
    void addEndObserver(CloudClientEndObserver observer);
    void runFiles() throws CloudClientListFilesException;
    void downloadFile(CompressionFile compressionFile) throws CloudClientDownloadException;
    void uploadFile(CompressionFile compressionFile) throws CloudClientUploadException;

    /**
     * Looks up {@code compressionFile}'s revision history for the earliest revision whose mime
     * type starts with {@code expectedMimeSuperType} (e.g. {@code "image"}), and restores it as
     * the file's current content. Used to recover files whose current content/mime type was
     * overwritten by an unrelated file's compression output. When {@code dryRun} is
     * {@code true}, only logs what would be restored, without changing anything.
     *
     * @return {@code true} if a matching revision was found (whether or not it was restored)
     */
    boolean restoreOriginalRevision(CompressionFile compressionFile, String expectedMimeSuperType, boolean dryRun)
            throws CloudClientRestoreException;
}
