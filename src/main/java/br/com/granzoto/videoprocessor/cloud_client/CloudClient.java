package br.com.granzoto.videoprocessor.cloud_client;

import java.io.File;
import java.util.List;

import br.com.granzoto.videoprocessor.model.VideoCompressionFile;

public interface CloudClient {

    public List<VideoCompressionFile> listFiles(String page) throws CloudClientListFilesException;
    public void downloadVideo(VideoCompressionFile compressionFile, File inputFile) throws CloudClientDownloadException;
    public void uploadVideo(File outputFile, VideoCompressionFile compressionFile) throws CloudClientUploadException;

}
