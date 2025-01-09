package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientUploadException;
import br.com.granzoto.media_compressor.compressor_strategy.CompressorFactory;
import br.com.granzoto.media_compressor.compressor_strategy.CompressorStrategy;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Workflow {
    private static final Logger LOGGER = Logger.getLogger(Workflow.class.getName());

    private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Downloads",
            "Original_Files");
    private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Downloads",
            "Compressed_Files");

    private final List<String> compressedFileNames = new ArrayList<>();
    private final CloudClient cloudClient;

    public static synchronized Workflow getInstance(CloudClient cloudClient) {
        return new Workflow(cloudClient);
    }

    public Workflow(CloudClient cloudClient) {
        if (Objects.isNull(cloudClient)) {
            throw new IllegalArgumentException("Cloud client must not be null");
        }
        this.cloudClient = cloudClient;
    }

    public final void run() throws CloudClientListFilesException {
        List<CompressionFile> compressionFileList = cloudClient.listFiles();

        if (Objects.nonNull(compressionFileList) && !compressionFileList.isEmpty()) {
            compressionFileList.forEach(compressionFile -> {
                if (!compressedFileNames.contains(compressionFile.name())) {
                    try {
                        LOGGER.info("BOF: " + compressionFile.name());
                        this.processMedia(compressionFile);
                        compressedFileNames.add(compressionFile.name());
                    } catch (WorkflowDownloadStepException d) {
                        LOGGER.warning("Download fail: " + compressionFile.name());
                    } catch (WorkflowCompressionStepException c) {
                        LOGGER.warning("Compression fail: " + compressionFile.name());
                    } catch (WorkflowUploadStepException u) {
                        LOGGER.warning("Upload fail: " + compressionFile.name());
                    }
                    LOGGER.info("EOF: " + compressionFile.name());
                } else {
                    LOGGER.info(compressionFile.name() + " already compressed. Skipping file.");
                }
            });
        } else {
            LOGGER.info("No files found.");
        }
    }

    private void processMedia(CompressionFile compressionFile)
            throws WorkflowDownloadStepException, WorkflowCompressionStepException,
            WorkflowUploadStepException {
        var inputFile = Path.of(DOWNLOAD_PATH.toString(),
                        compressionFile.mimeSuperType(),
                        compressionFile.name())
                .toFile();
        var outputFile = Path.of(UPLOAD_PATH.toString(),
                        compressionFile.mimeSuperType(),
                        inputFile.getName())
                .toFile();
        makeDirs(inputFile);
        makeDirs(outputFile);
        downloadMedia(compressionFile, inputFile);
        compressMedia(inputFile, outputFile, compressionFile.mimeSuperType());
        uploadMedia(outputFile, compressionFile);
    }

    private void makeDirs(File file) throws WorkflowCompressionStepException {
        try {
            FileUtils.createParentDirectories(file);
        } catch (IOException e) {
            throw new WorkflowCompressionStepException("Unable to create directories for: " + file.getAbsolutePath(), e);
        }
    }

    private void downloadMedia(CompressionFile compressionFile, File inputFile)
            throws WorkflowDownloadStepException {
        File downloadFile = inputFile;
        if (inputFile.exists()) {
            try {
                File duplicateDirectory = Path.of(inputFile.getParentFile().getPath(),
                                FilenameUtils.getBaseName(inputFile.getName()))
                        .toFile();
                FileUtils.createParentDirectories(duplicateDirectory);
                downloadFile = Path.of(duplicateDirectory.getPath(),
                                compressionFile.id() + "-" + compressionFile.name())
                        .toFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            LOGGER.warning("Download file already exists, duplicate file created at " + downloadFile.getAbsolutePath());
        }

        try {
            this.cloudClient.downloadFile(compressionFile, downloadFile);
        } catch (CloudClientDownloadException e) {
            throw new WorkflowDownloadStepException("Download from Google Drive failed", e);
        }
    }

    private CompressorStrategy getCompressor(String mimeSuperType) throws WorkflowCompressionStepException {
        CompressorStrategy compressor = CompressorFactory.getCompressorForMimeType(mimeSuperType);
        if (Objects.isNull(compressor)) {
            throw new WorkflowCompressionStepException(
                    "Compressor strategy for MimeType not found: " + mimeSuperType);
        }
        return compressor;
    }

    private void compressMedia(@Nonnull File inputFile, @Nonnull File outputFile, @Nonnull String mimeSuperType)
            throws WorkflowCompressionStepException {
        if (!outputFile.exists()) {
            var compressor = this.getCompressor(mimeSuperType);
            if (!compressor.executeCompression(inputFile, outputFile)) {
                throw new WorkflowCompressionStepException("Media compression failed");
            }
        } else {
            LOGGER.info("Output file already exists, skipping compression step.");
        }
    }

    private void uploadMedia(File outputFile, CompressionFile compressionFile)
            throws WorkflowUploadStepException {
        try {
            this.cloudClient.uploadFile(outputFile, compressionFile);
        } catch (CloudClientUploadException e) {
            throw new WorkflowUploadStepException("Upload to Google Drive Failed", e);
        }
    }

}
