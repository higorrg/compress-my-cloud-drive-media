package br.com.granzoto.media_compressor.cloud_client;

import java.io.File;
import java.util.List;

import br.com.granzoto.media_compressor.model.CompressionFile;

public interface CloudClient {

    public List<CompressionFile> listFiles() throws CloudClientListFilesException;
    public void downloadFile(CompressionFile compressionFile, File inputFile) throws CloudClientDownloadException;
    public void uploadFile(File outputFile, CompressionFile compressionFile) throws CloudClientUploadException;
    public void delete(CompressionFile myFile) throws CloudClientDeleteException;

}
