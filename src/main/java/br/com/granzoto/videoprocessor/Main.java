package br.com.granzoto.videoprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import br.com.granzoto.videoprocessor.cloud_client.CloudClient;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.videoprocessor.cloud_client.GoogleDriveClient;
import br.com.granzoto.videoprocessor.core.video_compressing.GoogleDriveVideoProcessor;
import br.com.granzoto.videoprocessor.core.video_compressing.VideoProcessingCompressionStepException;
import br.com.granzoto.videoprocessor.core.video_compressing.VideoProcessingDownloadStepException;
import br.com.granzoto.videoprocessor.core.video_compressing.VideoProcessingTemplate;
import br.com.granzoto.videoprocessor.core.video_compressing.VideoProcessingUploadStepException;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.FFmpegCompressorWithHost;
import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private List<String> compressedFileNames = new ArrayList<>();

    public static void main(String... args) throws CloudClientListFilesException {
        new Main().listFilesAndProcess(null);
    }

    public void listFilesAndProcess(String nextPageToken) throws CloudClientListFilesException {

        CloudClient cloudClient = GoogleDriveClient.getInstance();
        VideoCompressor compressor = new FFmpegCompressorWithHost();
        VideoProcessingTemplate processor = new GoogleDriveVideoProcessor(compressor);
        List<VideoCompressionFile> cloudFiles = cloudClient.listFiles(nextPageToken);

        if (cloudFiles != null && !cloudFiles.isEmpty()) {
            cloudFiles.forEach(myFile -> {
                // file.entrySet().forEach(e -> {
                // System.out.println(e.getKey()+": "+e.getValue());
                // });
                LOGGER.info(myFile.name() + " (" + FileUtils.byteCountToDisplaySize(myFile.size()) + ")");
                if (!compressedFileNames.contains(myFile.name())) {
                    try {
                        processor.processVideo(myFile);
                    } catch (VideoProcessingDownloadStepException | VideoProcessingCompressionStepException
                            | VideoProcessingUploadStepException e) {
                        LOGGER.severe(e.getMessage());
                    }
                    LOGGER.info("-------------------------------------------------\n");
                } else {
                    LOGGER.info(myFile.name() + " already compressed. Skipping file.");
                }
            });
            // listFiles(result.getNextPageToken());
        } else {
            LOGGER.info("No files found.");
        }
    }
}
