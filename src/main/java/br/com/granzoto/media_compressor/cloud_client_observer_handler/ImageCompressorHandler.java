package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientHandler;
import br.com.granzoto.media_compressor.cloud_client.CloudClientItemObserver;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImageCompressorHandler implements CloudClientHandler, CloudClientItemObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCompressorHandler.class.getName());
    public static final String IMAGE_MIME_TYPE = "image";

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (IMAGE_MIME_TYPE.equals(compressionFile.mimeSuperType()) && !compressionFile.compressedFile().exists()) {
            boolean executeCompression = this.executeCompression(compressionFile.originalFile(), compressionFile.compressedFile());
            if (!executeCompression && compressionFile.compressedFile().exists()) {
                if (compressionFile.compressedFile().delete()) {
                    LOGGER.info("Invalid compressed file successfully deleted {}", compressionFile.compressedFile().getAbsolutePath());
                } else {
                    LOGGER.warn("Fail deleting invalid compressed file {}. Will try to delete on JVM exit", compressionFile.compressedFile().getAbsolutePath());
                    compressionFile.compressedFile().deleteOnExit();
                }
            }
        }
    }

    private boolean executeCompression(File inputFile, File outputFile) {
        try {
            FileUtils.createParentDirectories(outputFile);
            String[] cmd = {"ffmpeg",
                    "-y",
                    "-loglevel", "quiet",
                    "-i", inputFile.getAbsolutePath().replaceAll(" ", "\\ "),
                    "-q:v", "2",
                    outputFile.getAbsolutePath().replaceAll(" ", "\\ ")};
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("Image compression successfully finished {}", outputFile.getName());
                return true;
            } else {
                LOGGER.warn("Image compression failed: Exit code {}. File: {}", exitCode, inputFile.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Image compression failed: {}. File: {}", e.getMessage(), inputFile.getAbsolutePath());
            return false;
        }
    }

    @Override
    public void registerObserver(CloudClient cloudClient) {
        cloudClient.addItemObserver(this);
    }
}
