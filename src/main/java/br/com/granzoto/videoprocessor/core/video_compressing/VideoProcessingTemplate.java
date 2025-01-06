package br.com.granzoto.videoprocessor.core.video_compressing;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import br.com.granzoto.videoprocessor.model.VideoCompressionFile;

public abstract class VideoProcessingTemplate {

    private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Original_Videos");
    private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Compressed_Videos");

    public final void processVideo(VideoCompressionFile videoCompressionFile)
            throws VideoProcessingDownloadStepException, VideoProcessingCompressionStepException,
            VideoProcessingUploadStepException {
        var inputFile = Path.of(DOWNLOAD_PATH.toString(), videoCompressionFile.name()).toFile();
        var outputFile = Path.of(UPLOAD_PATH.toString(), inputFile.getName()).toFile();
        downloadVideo(videoCompressionFile, inputFile);
        compressVideo(inputFile, outputFile);
        uploadVideo(outputFile, videoCompressionFile);
    }

    protected abstract void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
            throws VideoProcessingDownloadStepException;

    protected abstract void compressVideo(File inputFile, File outputFile)
            throws VideoProcessingCompressionStepException;

    protected abstract void uploadVideo(File outputFile, VideoCompressionFile compressionFile)
            throws VideoProcessingUploadStepException;
}
