package br.com.granzoto.media_compressor.cloud_client;

import java.io.File;
import java.util.List;

import br.com.granzoto.media_compressor.model.CompressionFile;

public interface CloudClient {

    void addListObserver(CloudClietListItemObserver listItemObserver);
    List<CompressionFile> listFiles() throws CloudClientListFilesException;
    void downloadFile(CompressionFile compressionFile, File inputFile) throws CloudClientDownloadException;
    void uploadFile(File outputFile, CompressionFile compressionFile) throws CloudClientUploadException;
    void delete(CompressionFile myFile) throws CloudClientDeleteException;

}
