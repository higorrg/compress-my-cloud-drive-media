package br.com.granzoto.videoprocessor.workflow;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import br.com.granzoto.videoprocessor.model.VideoCompressionFile;

public abstract class WorkflowTemplate {

    private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Original_Videos");
    private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Compressed_Videos");

    public final void processVideo(VideoCompressionFile videoCompressionFile)
            throws WorkflowDownloadStepException, WorkflowCompressionStepException,
            WorkflowUploadStepException {
        var inputFile = Path.of(DOWNLOAD_PATH.toString(), videoCompressionFile.name()).toFile();
        var outputFile = Path.of(UPLOAD_PATH.toString(), inputFile.getName()).toFile();
        downloadVideo(videoCompressionFile, inputFile);
        compressVideo(inputFile, outputFile);
        uploadVideo(outputFile, videoCompressionFile);
    }

    protected abstract void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
            throws WorkflowDownloadStepException;

    protected abstract void compressVideo(File inputFile, File outputFile)
            throws WorkflowCompressionStepException;

    protected abstract void uploadVideo(File outputFile, VideoCompressionFile compressionFile)
            throws WorkflowUploadStepException;
}
