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
}
