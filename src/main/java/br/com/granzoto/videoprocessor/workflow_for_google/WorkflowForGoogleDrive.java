package br.com.granzoto.videoprocessor.workflow_for_google;

import java.io.File;

import br.com.granzoto.videoprocessor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientUploadException;
import br.com.granzoto.videoprocessor.cloud_client.GoogleDriveClient;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;
import br.com.granzoto.videoprocessor.workflow.WorkflowCompressionStepException;
import br.com.granzoto.videoprocessor.workflow.WorkflowDownloadStepException;
import br.com.granzoto.videoprocessor.workflow.WorkflowTemplate;
import br.com.granzoto.videoprocessor.workflow.WorkflowUploadStepException;

public class WorkflowForGoogleDrive extends WorkflowTemplate {

    private final VideoCompressor compressor;

    public WorkflowForGoogleDrive(VideoCompressor compressor) {
        if (compressor == null) {
            throw new IllegalArgumentException("Compressor must not be null");
        }
        this.compressor = compressor;
    }

    @Override
    protected void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
            throws WorkflowDownloadStepException {
        try {
            GoogleDriveClient.getInstance().downloadVideo(compressionFile, inputFile);
        } catch (CloudClientDownloadException e) {
            throw new WorkflowDownloadStepException("Download from Google Drive failed", e);
        }
    }

    @Override
    protected void compressVideo(File inputFile, File outputFile) throws WorkflowCompressionStepException {
        if (!compressor.executeCompression(inputFile, outputFile)) {
            throw new WorkflowCompressionStepException("Video compression failed");
        }
    }

    @Override
    protected void uploadVideo(File outputFile, VideoCompressionFile compressionFile)
            throws WorkflowUploadStepException {

        try {
            GoogleDriveClient.getInstance().uploadVideo(outputFile, compressionFile);
        } catch (CloudClientUploadException e) {
            throw new WorkflowUploadStepException("Upload to Google Drive Failed", e);
        }

    }
}
