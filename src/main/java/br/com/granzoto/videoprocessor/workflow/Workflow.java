package br.com.granzoto.videoprocessor.workflow;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import br.com.granzoto.videoprocessor.cloud_client.CloudClient;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientUploadException;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;

public class Workflow {
        private static final Logger LOGGER = Logger.getLogger(Workflow.class.getName());

        private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos",
                        "Original_Videos");
        private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos",
                        "Compressed_Videos");

        private List<String> compressedFileNames = new ArrayList<>();
        private CloudClient cloudClient;
        private VideoCompressor compressor;

        public static synchronized Workflow getInstance(CloudClient cloudClient, VideoCompressor videoCompressor) {
                return new Workflow(cloudClient, videoCompressor);
        }

        public Workflow(CloudClient cloudClient, VideoCompressor videoCompressor) {
                if (Objects.isNull(cloudClient)) {
                        throw new IllegalStateException("Cloud client must not be null");
                }
                if (Objects.isNull(videoCompressor)) {
                        throw new IllegalStateException("Compressor must not be null");
                }
                this.cloudClient = cloudClient;
                this.compressor = videoCompressor;
        }

        public final void run() throws CloudClientListFilesException {
                List<VideoCompressionFile> cloudFiles = cloudClient.listFiles();
                List<VideoCompressionFile> downloadFail = new ArrayList<>();
                List<VideoCompressionFile> compressFail = new ArrayList<>();
                List<VideoCompressionFile> uploadFail = new ArrayList<>();

                if (cloudFiles != null && !cloudFiles.isEmpty()) {
                        cloudFiles.forEach(myFile -> {
                                if (!compressedFileNames.contains(myFile.name())) {
                                        try {
                                                LOGGER.info(myFile.name() + " ("
                                                                + FileUtils.byteCountToDisplaySize(myFile.size())
                                                                + ")");
                                                this.processVideo(myFile);
                                        } catch (WorkflowDownloadStepException d) {
                                                downloadFail.add(myFile);
                                                LOGGER.warning("Download fail: " + myFile.name());
                                                d.printStackTrace();
                                        } catch (WorkflowCompressionStepException c) {
                                                compressFail.add(myFile);
                                                LOGGER.warning("Compression fail: " + myFile.name());
                                                c.printStackTrace();
                                        } catch (WorkflowUploadStepException u) {
                                                uploadFail.add(myFile);
                                                LOGGER.warning("Upload fail: " + myFile.name());
                                                u.printStackTrace();
                                        }
                                        LOGGER.info("-------------------------------------------------\n");
                                } else {
                                        LOGGER.info(myFile.name() + " already compressed. Skipping file.");
                                }
                        });
                        if (downloadFail.size() > 0) {
                                LOGGER.warning("Download Fail List:");
                                downloadFail.forEach(f -> System.out.println(f.name()));
                        }
                        if (compressFail.size() > 0) {
                                LOGGER.warning("Compression Fail List:");
                                compressFail.forEach(f -> System.out.println(f.name()));
                        }

                        if (uploadFail.size() > 0) {
                                LOGGER.warning("Upload Fail List:");
                                uploadFail.forEach(f -> System.out.println(f.name()));
                        }

                } else {
                        LOGGER.info("No files found.");
                }
        }

        private final void processVideo(VideoCompressionFile videoCompressionFile)
                        throws WorkflowDownloadStepException, WorkflowCompressionStepException,
                        WorkflowUploadStepException {
                var inputFile = Path.of(DOWNLOAD_PATH.toString(), videoCompressionFile.name()).toFile();
                var outputFile = Path.of(UPLOAD_PATH.toString(), inputFile.getName()).toFile();
                downloadVideo(videoCompressionFile, inputFile);
                compressVideo(inputFile, outputFile);
                uploadVideo(outputFile, videoCompressionFile);
        }

        private void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
                        throws WorkflowDownloadStepException {
                if (!inputFile.exists()) {
                        try {
                                this.cloudClient.downloadVideo(compressionFile, inputFile);
                        } catch (CloudClientDownloadException e) {
                                throw new WorkflowDownloadStepException("Download from Google Drive failed", e);
                        }
                } else {
                        LOGGER.warning("Download file already exists, skipping download step.");
                }
        }

        private void compressVideo(File inputFile, File outputFile) throws WorkflowCompressionStepException {
                if (!outputFile.exists()) {
                        if (!this.compressor.executeCompression(inputFile, outputFile)) {
                                throw new WorkflowCompressionStepException("Video compression failed");
                        }
                } else {
                        LOGGER.info("Outputfile already exists, skipping compression step.");
                }
        }

        private void uploadVideo(File outputFile, VideoCompressionFile compressionFile)
                        throws WorkflowUploadStepException {
                try {
                        this.cloudClient.uploadVideo(outputFile, compressionFile);
                } catch (CloudClientUploadException e) {
                        throw new WorkflowUploadStepException("Upload to Google Drive Failed", e);
                }
        }

}
