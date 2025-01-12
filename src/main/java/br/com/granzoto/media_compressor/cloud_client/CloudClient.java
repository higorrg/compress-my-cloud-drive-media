package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

public interface CloudClient {

    CloudClient addHandler(CloudClientHandler handler);
    void runFiles() throws CloudClientListFilesException;
    void downloadFile(CompressionFile compressionFile) throws CloudClientDownloadException;
    void uploadFile(CompressionFile compressionFile) throws CloudClientUploadException;
}
