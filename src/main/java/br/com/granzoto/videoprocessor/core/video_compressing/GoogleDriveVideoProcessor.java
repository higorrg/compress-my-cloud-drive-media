package br.com.granzoto.videoprocessor.core.video_compressing;

import java.io.File;

import br.com.granzoto.videoprocessor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientUploadException;
import br.com.granzoto.videoprocessor.cloud_client.GoogleDriveClient;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;

public class GoogleDriveVideoProcessor extends VideoProcessingTemplate {

    private final VideoCompressor compressor;

    public GoogleDriveVideoProcessor(VideoCompressor compressor) {
        if (compressor == null) {
            throw new IllegalArgumentException("Compressor must not be null");
        }
        this.compressor = compressor;
    }

    @Override
    protected void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
            throws VideoProcessingDownloadStepException {
        try {
            GoogleDriveClient.getInstance().downloadVideo(compressionFile, inputFile);
        } catch (CloudClientDownloadException e) {
            throw new VideoProcessingDownloadStepException("Download from Google Drive failed", e);
        }
    }

    @Override
    protected void compressVideo(File inputFile, File outputFile) throws VideoProcessingCompressionStepException {
        if (!compressor.executeCompression(inputFile, outputFile)) {
            throw new VideoProcessingCompressionStepException("Video compression failed");
        }
    }

    @Override
    protected void uploadVideo(File outputFile, VideoCompressionFile compressionFile)
            throws VideoProcessingUploadStepException {

        try {
            GoogleDriveClient.getInstance().uploadVideo(outputFile, compressionFile);
        } catch (CloudClientUploadException e) {
            throw new VideoProcessingUploadStepException("Upload to Google Drive Failed", e);
        }

    }
}
